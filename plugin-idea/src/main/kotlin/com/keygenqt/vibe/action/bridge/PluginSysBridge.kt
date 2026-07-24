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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * IntelliJ plugin implementation of the system bridge.
 * Maps notifications and dialogs to native IntelliJ Platform APIs.
 */
class PluginSysBridge(val project: Project) : SysBridge {

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
     */
    override val runCli: ((args: List<String>, onEvent: (String) -> Unit, onDone: (Int) -> Unit) -> Unit) =
        { args, onEvent, onDone ->
            val commandLine = if (SystemInfo.isUnix) {
                val shell = System.getenv("SHELL")?.takeIf { it.isNotBlank() } ?: "/bin/sh"
                val cmd = buildString {
                    append("VIBE_LOG_TYPE=json \"$cliPath\"")
                    args.forEach { append(" \"$it\"") }
                }
                GeneralCommandLine(shell, "-lc", cmd)
            } else {
                GeneralCommandLine(cliPath).withParameters(args)
                    .withEnvironment("VIBE_LOG_TYPE", "json")
            }

            val handler = OSProcessHandler(commandLine)
            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType == ProcessOutputTypes.STDOUT) {
                        event.text.lines()
                            .filter { it.isNotBlank() }
                            .forEach { onEvent(it) }
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    onDone(event.exitCode)
                }
            })
            handler.startNotify()
        }

    /**
     * Opens a file in the IntelliJ editor.
     */
    override val openFile: ((path: String) -> Unit) = { path ->
        val file = LocalFileSystem.getInstance().findFileByPath(path)
        if (file != null) {
            FileEditorManager.getInstance(project).openFile(file, true)
        }
    }
}
