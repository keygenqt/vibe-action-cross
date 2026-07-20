/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages

/**
 * IntelliJ plugin implementation of the system bridge.
 * Maps notifications and dialogs to native IntelliJ Platform APIs.
 */
class PluginSysBridge(val project: Project) : SysBridge {

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
}
