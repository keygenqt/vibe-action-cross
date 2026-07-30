/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action

import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Project-scoped registry of live vibe-action CLI processes.
 * Kills any still-running CLI when the project is closed, so a process
 * never outlives the IDE window that started it.
 */
@Service(Service.Level.PROJECT)
class CliProcessService : Disposable {

    private val liveHandlers = ConcurrentHashMap.newKeySet<OSProcessHandler>()

    fun track(handler: OSProcessHandler) {
        liveHandlers.add(handler)
    }

    fun untrack(handler: OSProcessHandler) {
        liveHandlers.remove(handler)
    }

    override fun dispose() {
        liveHandlers.forEach { it.destroyProcess() }
        liveHandlers.clear()
    }
}
