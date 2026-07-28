/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.datatransfer.DataFlavor
import java.io.File

/**
 * IntelliJ plugin implementation of the system bridge.
 * Maps notifications and dialogs to native IntelliJ Platform APIs.
 */
class PluginSysBridge(val project: Project) : SysBridge {

    /** Guards against concurrent runCli calls — only one CLI process at a time. */
    @Volatile
    private var cliBusy = false

    /**
     * Path to vibe-action CLI binary. Uses VIBE_ACTION_CLI_PATH env var for debug builds,
     * falls back to system PATH lookup.
     */
    private val cliPath: String = System.getenv("VIBE_ACTION_CLI_PATH") ?: "vibe-action"

    /**
     * Dispatches a native IntelliJ notification balloon.
     */
    override val showNotification: ((title: String, message: String) -> Unit) = { title, message ->
        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.keygenqt.vibe.action")
            .createNotification(title, message, NotificationType.INFORMATION)
            .notify(project)
    }

    /**
     * Invokes a native IntelliJ OK/Cancel confirmation dialog.
     */
    override val showConfirmDialog: ((title: String, message: String, onResult: (Boolean) -> Unit) -> Unit) =
        { title, message, onResult ->
            onResult(
                MessageDialogBuilder.okCancel(title, message)
                    .icon(Messages.getQuestionIcon())
                    .ask(project),
            )
        }

    /**
     * IDEA doesn't need an explicit theme-change hook — Jewel already
     * recomposes JewelTheme.isDark/globalColors live via SwingBridgeTheme
     * when the IDE theme changes, so platformColorScheme() picks it up on its own.
     */
    override val onThemeChanged: ((onChanged: () -> Unit) -> (() -> Unit))? = null

    /**
     * Runs vibe-action CLI process, streams NDJSON stdout lines to onEvent.
     * Uses login shell to inherit full PATH on Unix (macOS GUI apps get stripped PATH).
     *
     * Only one CLI process may run at a time (same contract as VsCodeSysBridge):
     * a concurrent call throws [IllegalStateException]. Lines are buffered and
     * emitted only when complete — the platform delivers stdout in arbitrary
     * chunks, so a JSON line may be split across events. The trailing line
     * without '\n' is flushed on process termination.
     */
    override val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> Unit) =
        { args, onEvent, onDone ->
            check(!cliBusy) { "Concurrent runCli is not supported by PluginSysBridge" }
            cliBusy = true

            val commandLine = if (SystemInfo.isUnix) {
                val shell = System.getenv("SHELL")?.takeIf { it.isNotBlank() } ?: "/bin/sh"
                val cmd = buildString {
                    append("VIBE_LOG_TYPE=json ")
                    append(cliPath.shellEscape())
                    args.forEach { append(' '); append(it.shellEscape()) }
                }
                GeneralCommandLine(shell, "-lc", cmd)
            } else {
                GeneralCommandLine(cliPath).withParameters(args)
                    .withEnvironment("VIBE_LOG_TYPE", "json")
            }.withCharset(Charsets.UTF_8)

            // Local per invocation — parallel runCli calls must not share the buffer.
            // Listener events are dispatched sequentially on the process reader thread,
            // so no synchronization is needed here.
            val stdoutBuffer = StringBuilder()

            val handler = try {
                OSProcessHandler(commandLine)
            } catch (e: Exception) {
                cliBusy = false
                throw e
            }
            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType != ProcessOutputTypes.STDOUT) return
                    stdoutBuffer.append(event.text)
                    var idx = stdoutBuffer.indexOf('\n')
                    while (idx >= 0) {
                        val line = stdoutBuffer.substring(0, idx).trimEnd('\r')
                        stdoutBuffer.delete(0, idx + 1)
                        if (line.isNotBlank()) onEvent(line)
                        idx = stdoutBuffer.indexOf('\n')
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    val tail = stdoutBuffer.toString().trim()
                    if (tail.isNotEmpty()) onEvent(tail)
                    // Release the slot before notifying — the bridge is reusable immediately after.
                    cliBusy = false
                    onDone(event.exitCode)
                }
            })
            handler.startNotify()
        }

    /**
     * Opens a file in the IntelliJ editor. Refreshes VFS from disk first —
     * the file may have been created or changed externally by the CLI,
     * in which case the cached [LocalFileSystem.findFileByPath] would return null.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        val file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(path))
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true)
        }
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
     * Returns the currently selected text editor from FileEditorManager, or null if none.
     */
    private fun getActiveEditor(): Editor? {
        return FileEditorManager.getInstance(project).selectedTextEditor
    }
}

/**
 * Escapes a string for safe embedding into a POSIX shell command line:
 * wraps in single quotes, escapes embedded quotes as '\''.
 */
private fun String.shellEscape(): String = "'" + replace("'", "'\\''") + "'"
