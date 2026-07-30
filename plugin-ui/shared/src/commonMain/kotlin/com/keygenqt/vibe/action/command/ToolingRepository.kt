/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import co.touchlab.kermit.Logger
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException

/**
 * Repository for tooling and maintenance tasks.
 * Handles CLI commands not related to pipelines (e.g., status, clean).
 */
class ToolingRepository(
    private val commandProvider: CommandProvider,
    private val logger: Logger,
) {
    companion object {
        @Volatile
        private var cachedStatus: CommandOutput.Status? = null
    }

    /**
     * Checks if the CLI status has already been fetched and cached.
     * Used by the ViewModel to decide whether to show a loading spinner.
     */
    fun isCached(): Boolean = cachedStatus != null

    /**
     * Fetches the CLI status. Caches the result to avoid multiple CLI calls.
     * Pass forceRefresh = true to bypass the cache.
     */
    suspend fun getStatus(forceRefresh: Boolean = false): CommandOutput.Status? {
        if (cachedStatus != null && !forceRefresh) {
            return cachedStatus
        }
        return try {
            commandProvider.status()
                .filterIsInstance<CommandOutput.Status>()
                .firstOrNull()
                .also { cachedStatus = it }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Status command failed, cannot get config path" }
            null
        }
    }

    /**
     * Runs the 'clean' command to remove all cache.
     * Throws an exception if the command fails.
     */
    suspend fun cleanCache() {
        try {
            commandProvider.execute(listOf("clean"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.w(e) { "Clean command failed" }
            throw e
        }
    }
}
