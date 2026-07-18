package com.keygenqt.vibe.action.di

/**
 * Lock object for synchronized console output.
 */
private val printLock = Any()

/**
 * Prints synchronously on JVM to avoid interleaved log lines.
 */
internal actual fun synchronizedPrint(block: () -> Unit) {
    synchronized(printLock) {
        block()
    }
}
