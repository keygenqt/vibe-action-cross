/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.command

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.bridge.CliProcess
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.command.CommandProvider.Companion.EXIT_SUPERSEDED
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

/**
 * Provider that executes CLI commands and returns parsed output.
 */
class CommandProvider(
    private val env: Environment,
    private val logger: Logger,
) {
    private val runCli get() = env.bridge.sys.runCli ?: error("runCli is not available")

    /**
     * Executes 'status' command and returns list of outputs.
     */
    suspend fun status(): List<CommandOutput> = execute(listOf("status"))

    /**
     * Executes default command (actions list) and returns parsed outputs.
     */
    suspend fun actions(): List<CommandOutput> = execute(listOf())

    /**
     * General command execution, suspending until the CLI process finishes.
     *
     * Cancellation kills the underlying OS process via the [CliProcess]
     * handle; the bridge drops all late events from it afterward.
     *
     * Exit code [EXIT_SUPERSEDED] (process shut down because a newer CLI
     * instance took over) is mapped to [CancellationException] so the UI
     * reports "canceled" rather than "failed".
     */
    suspend fun execute(args: List<String>): List<CommandOutput> {
        val cmdString = args.ifEmpty { listOf("default") }.joinToString(" ")
        try {
            return withTimeout(CLI_TIMEOUT) {
                suspendCancellableCoroutine { cont ->
                    val result = mutableListOf<CommandOutput>()
                    try {
                        val process = runCli(
                            args,
                            { line ->
                                // Late events may race with cancellation — drop them.
                                logger.d(line)
                                if (cont.isActive) result.add(parseCommandOutputLine(line))
                            },
                            { exitCode, streams ->
                                if (cont.isActive) {
                                    val (stdout, stderr) = streams
                                    when (exitCode) {
                                        0 -> cont.resume(result.toList())
                                        EXIT_SUPERSEDED -> {
                                            logger.w("Command '$cmdString' was superseded by a newer CLI process")
                                            cont.resumeWithException(
                                                CancellationException(
                                                    "Command '$cmdString' was superseded by a newer CLI process",
                                                ),
                                            )
                                        }

                                        else -> {
                                            val message = result.filterIsInstance<CommandOutput.Fallback>()
                                                .firstOrNull { it.level == "error" }
                                                ?.value?.get("message")?.toString()

                                            val rawError = stderr.ifBlank { stdout }
                                            val error = if (rawError.isBlank()) {
                                                "Command '$cmdString' failed with exit code $exitCode."
                                            } else {
                                                """
                                                |Command '$cmdString' failed with exit code $exitCode.
                                                |--- output ---
                                                |$rawError
                                                """.trimMargin()
                                            }
                                            error.trim().lines().filter { it.isNotBlank() }.forEach { logger.e(it) }
                                            cont.resumeWithException(RuntimeException(message ?: error))
                                        }
                                    }
                                }
                            },
                        )
                        // Registered after runCli returns; if the coroutine was
                        // already canceled, the handler fires immediately.
                        cont.invokeOnCancellation { runCatching { process.cancel() } }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Platform bridge may throw synchronously (e.g. IntelliJ throws
                        // ExecutionException when the CLI binary is not found in PATH).
                        // Resume here — the outer resumeWithException would throw
                        // IllegalStateException on an already-resumed continuation.
                        logger.e("Failed to start CLI command '$cmdString'", e)
                        if (cont.isActive) cont.resumeWithException(e)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            // invokeOnCancellation above has already killed the process —
            // no lingering CLI is left behind.
            logger.w("Command '$cmdString' timed out after $CLI_TIMEOUT")
            throw RuntimeException(
                "Command '$cmdString' timed out after $CLI_TIMEOUT",
                e,
            )
        }
    }

    /**
     * Parses a single line of CLI output.
     * Uses the presence of the discriminator key inside the "value" object
     * to decide between typed (Status/Actions) and untyped (Fallback) decoding.
     */
    private fun parseCommandOutputLine(line: String): CommandOutput {
        val obj = runCatching { commandJson.parseToJsonElement(line).jsonObject }.getOrNull()
            ?: return CommandOutput.Unknown

        val hasDiscriminator = (obj["value"] as? JsonObject)?.containsKey(API_DISCRIMINATOR) == true

        if (hasDiscriminator) {
            runCatching { return commandJsonExport.decodeFromJsonElement(CommandEnvelope.serializer(), obj).value }
                .onFailure { logger.w(it) { "Discriminator present but decode failed: $line" } }
        }

        return runCatching { commandJson.decodeFromJsonElement(CommandOutput.Fallback.serializer(), obj) }
            .getOrElse {
                logger.w(it) { "Failed to parse line: $line" }
                CommandOutput.Unknown
            }
    }

    private companion object {
        val CLI_TIMEOUT = 60.seconds

        /**
         * Exit code used by vibe-action's RunGuard when a newer instance
         * takes over and this process shuts itself down.
         * Must match EXIT_SUPERSEDED in run_guard.rs.
         */
        const val EXIT_SUPERSEDED = 130
    }
}
