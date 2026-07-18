package com.keygenqt.vibe.action

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.util.SystemInfo

/**
 * Plugin lifecycle listener for IDE startup events.
 */
class PluginLifecycle : AppLifecycleListener {
    /**
     * Called when the IDE main frame is created.
     */
    override fun appFrameCreated(commandLineArgs: List<String>) {
        if (SystemInfo.isMac) {
            System.setProperty("skiko.renderApi", "SOFTWARE")
            System.setProperty("skiko.metal.enabled", "false")
        }
    }
}
