/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * Low-level bridge to the VS Code extension host via postMessage,
 * correlating request/response pairs by requestId for calls that need a result.
 */
class VsCodeApi(private val raw: dynamic) {

    private var nextRequestId = 0
    private val pendingCallbacks = mutableMapOf<Int, (dynamic) -> Unit>()

    /** Requests whose callback was never invoked — logged periodically, not on every send. */
    private var lastPendingLogSize = 0

    init {
        window.addEventListener("message", { event: Event ->
            val data = event.asDynamic().data ?: return@addEventListener
            val requestId = data.requestId.unsafeCast<Int?>()
            if (requestId != null) {
                pendingCallbacks.remove(requestId)?.invoke(data.result)
            }
        })
    }

    /**
     * Sends a command to the VS Code extension host, optionally awaiting a correlated result.
     */
    fun send(target: String, args: Array<Any?>, onResult: ((dynamic) -> Unit)? = null) {
        val requestId = onResult?.let { ++nextRequestId }
        if (onResult != null && requestId != null) {
            pendingCallbacks[requestId] = onResult
        }

        // Leak detector: a callback is removed only when the host replies.
        // If the host handler fails to post a response, entries accumulate silently.
        if (pendingCallbacks.size > lastPendingLogSize + PENDING_LOG_STEP) {
            console.warn("VsCodeApi: ${pendingCallbacks.size} pending callbacks — possible response leak")
            lastPendingLogSize = pendingCallbacks.size
        }

        raw.postMessage(
            kotlin.js.json(
                Pair("target", target),
                Pair("args", args),
                Pair("requestId", requestId),
            ),
        )
    }

    private companion object {
        const val PENDING_LOG_STEP = 10
    }
}
