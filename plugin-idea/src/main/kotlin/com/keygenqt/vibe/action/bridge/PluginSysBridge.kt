/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import com.intellij.DynamicBundle
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.keygenqt.vibe.action.CliProcessService
import com.keygenqt.vibe.action.resources.MessageBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Dimension
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

/**
 * IntelliJ plugin implementation of the system bridge.
 * Maps notifications and dialogs to native IntelliJ Platform APIs.
 */
class PluginSysBridge(val project: Project) : SysBridge {

    /**
     * Locale language code, or null if blank.
     */
    override val language: String?
        get() = DynamicBundle.getLocale().language.takeIf { it.isNotBlank() }

    /**
     * Service for managing CLI process lifecycle and communication within the project.
     */
    private val cliProcesses = project.service<CliProcessService>()

    /**
     * Path to vibe-action CLI binary. Uses VIBE_ACTION_CLI_PATH env var for debug builds,
     * falls back to system PATH lookup.
     */
    private val cliPath: String = System.getenv("VIBE_ACTION_CLI_PATH") ?: "vibe-action"

    /**
     * Dispatches a native IntelliJ notification balloon.
     */
    override val showNotification: ((title: String, message: String) -> Unit) = { title, message ->
        if (!project.isDisposed) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("com.keygenqt.vibe.action")
                .createNotification(title, message, NotificationType.INFORMATION)
                .notify(project)
        }
    }

    /**
     * Invokes a native IntelliJ OK/Cancel confirmation dialog.
     * Uses invokeLater — the dialog is async, the callback fires on EDT
     * after the user responds. Must not block the process reader thread.
     */
    override val showConfirmDialog: ((title: String, message: String, onResult: (Boolean) -> Unit) -> Unit) =
        { title, message, onResult ->
            ApplicationManager.getApplication().invokeLater {
                if (project.isDisposed) {
                    onResult(false)
                    return@invokeLater
                }
                onResult(
                    MessageDialogBuilder.okCancel(title, message)
                        .icon(Messages.getQuestionIcon())
                        .ask(project),
                )
            }
        }

    /**
     * IDEA doesn't need an explicit theme-change hook — Jewel already
     * recomposes JewelTheme.isDark/globalColors live via SwingBridgeTheme
     * when the IDE theme changes, so platformColorScheme() picks it up on its own.
     */
    override val onThemeChanged: ((onChanged: () -> Unit) -> (() -> Unit))? = null

    /**
     * Runs vibe-action CLI process, streams NDJSON stdout lines to onEvent.
     * Uses login shell to inherit full PATH on Unix (macOS GUI apps get stripped
     * PATH); `exec` replaces the shell with the CLI so destroyProcess() kills
     * the CLI itself, not an orphaned shell wrapper.
     *
     * Concurrent calls are supported: every invocation owns its process, buffer
     * and event stream — there is no shared slot. Sequencing of *actions* is
     * handled outside the bridge: the ViewModel cancels the previous coroutine
     * (which cancels the returned [CliProcess]), and the Rust RunGuard kills
     * the previous action when a new one starts. This matters for guard-free
     * commands like `status`, which may run alongside an action.
     *
     * Lines are buffered and emitted only when complete — the platform delivers
     * stdout in arbitrary chunks, so a JSON line may be split across events.
     * The trailing line without '\n' is flushed on process termination.
     *
     * After [CliProcess.cancel] no further callbacks are delivered: finished
     * is the fence (a callback racing on the reader thread may still slip
     * through — CommandProvider guards those with isActive).
     */
    override val runCli: ((args: List<String>, onEvent: (String, (String) -> Unit) -> Unit, onDone: (Int, Pair<String, String>) -> Unit) -> CliProcess) =
        { args, onEvent, onDone ->
            val commandLine = if (SystemInfo.isUnix) {
                val shell = System.getenv("SHELL")?.takeIf { it.isNotBlank() } ?: "/bin/sh"
                val cmd = buildString {
                    // exec replaces the shell process with the CLI
                    append("exec ")
                    append(cliPath.shellEscape())
                    args.forEach {
                        append(' ')
                        append(it.shellEscape())
                    }
                }
                GeneralCommandLine(shell, "-lc", cmd)
                    .withEnvironment("VIBE_LOG_TYPE", "json")
            } else {
                GeneralCommandLine(cliPath).withParameters(args)
                    .withEnvironment("VIBE_LOG_TYPE", "json")
            }.withCharset(Charsets.UTF_8)

            val handler = OSProcessHandler(commandLine)
            cliProcesses.track(handler)

            // Flipped exactly once — by whichever finishes first:
            // processTerminated (natural exit: callbacks fire) or
            // CliProcess.cancel (destroy: callbacks are suppressed).
            val finished = AtomicBoolean(false)

            // Local per invocation. Listener events are dispatched sequentially
            // on the process reader thread, so the buffer needs no synchronization.
            val stdoutBuffer = StringBuilder()
            val outputBuffer = Pair(StringBuilder(), StringBuilder())

            // Stdin writer — available from the first event, eliminating the
            // race between event delivery and process handle assignment.
            val writeStdinFn: (String) -> Unit = { text ->
                if (!finished.get()) {
                    try {
                        handler.processInput.let { stream ->
                            stream.write(text.toByteArray())
                            stream.flush()
                        }
                    } catch (e: Exception) {
                        // Process may have already exited — broken pipe must not crash.
                    }
                }
            }

            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (finished.get()) return
                    when (outputType) {
                        ProcessOutputTypes.STDOUT -> {
                            stdoutBuffer.append(event.text)
                            outputBuffer.first.append(event.text)
                            var idx = stdoutBuffer.indexOf('\n')
                            while (idx >= 0) {
                                val line = stdoutBuffer.substring(0, idx).trimEnd('\r')
                                stdoutBuffer.delete(0, idx + 1)
                                if (line.isNotBlank()) onEvent(line, writeStdinFn)
                                idx = stdoutBuffer.indexOf('\n')
                            }
                        }

                        ProcessOutputTypes.STDERR -> {
                            outputBuffer.second.append(event.text)
                        }
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    cliProcesses.untrack(handler)
                    // Canceled — the caller explicitly asked for silence.
                    if (!finished.compareAndSet(false, true)) return
                    val tail = stdoutBuffer.toString().trim()
                    if (tail.isNotEmpty()) onEvent(tail, writeStdinFn)
                    onDone(event.exitCode, Pair(outputBuffer.first.toString(), outputBuffer.second.toString()))
                }
            })
            handler.startNotify()

            object : CliProcess {
                override fun cancel() {
                    if (finished.compareAndSet(false, true)) {
                        handler.destroyProcess()
                    }
                }

                override fun writeStdin(text: String) = writeStdinFn(text)
            }
        }

    /**
     * Opens a file in the IntelliJ editor. Refreshes VFS from disk first —
     * the file may have been created or changed externally by the CLI,
     * in which case the cached [LocalFileSystem.findFileByPath] would return null.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        ApplicationManager.getApplication().executeOnPooledThread {
            val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path) ?: return@executeOnPooledThread
            ApplicationManager.getApplication().invokeLater({
                FileEditorManager.getInstance(project).openFile(file, true)
            }, project.disposed)
        }
    }

    /**
     * Writes text content to a file on the local filesystem.
     */
    override val writeFile: (suspend (String, String) -> Unit) = { path, content ->
        withContext(Dispatchers.IO) {
            File(path).writeText(content)
        }
    }

    /**
     * Deletes a file from the local filesystem and synchronizes the VFS.
     * Note: Performs synchronous I/O. Must not be called on the EDT.
     */
    override val deleteFile: (suspend (String) -> Boolean) = { path ->
        withContext(Dispatchers.IO) { File(path).delete() }
    }

    /**
     * Checks if a file exists at the given absolute path (JVM-local filesystem).
     * Blocking I/O is moved off the calling thread (typically EDT).
     */
    override val fileExists: (suspend (String) -> Boolean) = { path ->
        withContext(Dispatchers.IO) { File(path).exists() }
    }

    /**
     * Retrieves the currently selected text from the active editor and passes it to the provided callback.
     */
    override val getSelectedText: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            val text = getActiveEditor()?.selectionModel?.selectedText
            onResult(text)
        }
    }

    /**
     * Retrieves the current text from the system clipboard.
     */
    override val getClipboardText: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            val text = CopyPasteManager.getInstance()
                .getContents<String?>(DataFlavor.stringFlavor)
            onResult(text)
        }
    }

    /**
     * Prompts the user for text input via a dialog and passes it to the provided handler.
     */
    override val getDialogText: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            val text = Messages.showInputDialog(
                project,
                MessageBundle.message("dialog.input.message"),
                MessageBundle.message("dialog.input.title"),
                Messages.getQuestionIcon(),
            )
            onResult(text)
        }
    }

    /**
     * Retrieves the path of the currently active file in the editor.
     */
    override val getCurrentFilePath: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) {
                onResult(null)
                return@invokeLater
            }
            onResult(FileEditorManager.getInstance(project).selectedEditor?.file?.path)
        }
    }

    /**
     * Retrieves the root path of the current project.
     */
    override val getProjectPath: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            onResult(project.basePath)
        }
    }

    /**
     * Retrieves the current cursor line number (1-indexed) in the active editor.
     */
    override val getCursorLine: (((String?) -> Unit) -> Unit) = { onResult ->
        ApplicationManager.getApplication().invokeLater {
            val line = getActiveEditor()?.caretModel?.logicalPosition?.line?.plus(1)?.toString()
            onResult(line)
        }
    }

    /**
     * Writes the given text to the system clipboard.
     */
    @Suppress("UsePropertyAccessSyntax")
    override val setClipboardText: ((String) -> Unit) = { newText ->
        ApplicationManager.getApplication().invokeLater {
            CopyPasteManager.getInstance().setContents(StringSelection(newText))
        }
    }

    /**
     * Replaces the currently selected text in the active editor with the given newText.
     */
    override val replaceSelectedText: ((String) -> Unit) = { newText ->
        ApplicationManager.getApplication().invokeLater {
            getActiveEditor()?.let { editor ->
                val selectionModel = editor.selectionModel
                WriteCommandAction.runWriteCommandAction(project) {
                    val start = selectionModel.selectionStart
                    val end = selectionModel.selectionEnd
                    editor.document.replaceString(start, end, newText)
                }
            }
        }
    }

    /**
     * Callback that shows a multiline text dialog/output to the user.
     */
    @Suppress("UsePropertyAccessSyntax")
    override val showTextDialog: ((title: String, text: String) -> Unit) = { title, text ->
        ApplicationManager.getApplication().invokeLater {
            object : DialogWrapper(project) {
                init {
                    this.title = title
                    init()
                }

                override fun createCenterPanel(): JComponent {
                    val textArea = JBTextArea(text).apply {
                        isEditable = false
                        lineWrap = true
                        wrapStyleWord = true
                    }
                    return JBScrollPane(textArea).apply {
                        preferredSize = Dimension(620, 350)
                    }
                }

                override fun createActions(): Array<Action> {
                    val copyAction = object : AbstractAction(MessageBundle.message("common.copy")) {
                        override fun actionPerformed(e: ActionEvent?) {
                            CopyPasteManager.getInstance().setContents(StringSelection(text))
                            close(CLOSE_EXIT_CODE)
                        }
                    }
                    return arrayOf(copyAction, okAction)
                }
            }.show()
        }
    }

    /**
     * Returns the currently selected text editor from FileEditorManager, or null if none.
     */
    private fun getActiveEditor(): Editor? = FileEditorManager.getInstance(project).selectedTextEditor

    /**
     * Loads a simple string preference from IntelliJ's application-level
     * persistent storage (PropertiesComponent).
     */
    override val loadPreference: (suspend (String) -> String?) = { key ->
        PropertiesComponent.getInstance().getValue(key)
    }

    /**
     * Saves a simple string preference to IntelliJ's application-level
     * persistent storage (PropertiesComponent).
     */
    override val savePreference: ((String, String) -> Unit) = { key, value ->
        PropertiesComponent.getInstance().setValue(key, value)
    }
}

/**
 * Escapes a string for safe embedding into a POSIX shell command line:
 * wraps in single quotes, escapes embedded quotes as '\''.
 */
private fun String.shellEscape(): String = "'" + replace("'", "'\\''") + "'"
