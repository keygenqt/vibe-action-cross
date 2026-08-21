/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.keygenqt.vibe.action.bridge.PluginEnvironment
import com.keygenqt.vibe.action.resources.IconBundle
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JComponent

/**
 * IntelliJ tool window factory for the plugin sidebar.
 */
@Suppress("UnstableApiUsage")
@OptIn(ExperimentalJewelApi::class)
class PluginSidebar : ToolWindowFactory {
    /**
     * Sidebar tool window icon.
     */
    override val icon: Icon = IconBundle.AppIcon

    /**
     * Tool window is always available.
     */
    override fun shouldBeAvailable(project: Project) = true

    /**
     * Creates the Compose-based sidebar content within the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val page = SidebarScreen(project)
        val content = ContentFactory.getInstance().createContent(page.getContent(), null, false)
        Disposer.register(content) { page.dispose() }
        toolWindow.contentManager.addContent(content)
    }

    /**
     * Sidebar screen hosting the Compose Multiplatform UI inside a Swing panel.
     */
    class SidebarScreen(private val project: Project) {

        /**
         * Compose panel with Jewel theme and plugin environment.
         */
        private val composePanel = ComposePanel().apply {
            setContent {
                val environment = remember(project) { PluginEnvironment(project) }
                SwingBridgeTheme {
                    InitApp(environment) {
                        RootAppDispatcher()
                    }
                }
            }
        }

        /**
         * Root Swing panel containing the Compose panel.
         */
        private val rootPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(composePanel, BorderLayout.CENTER)
        }

        /**
         * Returns the root Swing component for the tool window.
         */
        fun getContent(): JComponent = rootPanel

        /**
         * Disposes the Compose panel to release resources.
         */
        @OptIn(ExperimentalComposeUiApi::class)
        fun dispose() {
            composePanel.dispose()
        }
    }
}
