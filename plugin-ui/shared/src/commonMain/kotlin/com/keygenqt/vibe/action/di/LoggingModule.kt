package com.keygenqt.vibe.action.di

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Platform-specific synchronized print implementation.
 */
internal expect fun synchronizedPrint(block: () -> Unit)

/**
 * Time format for log entries: HH:mm:ss.SSS.
 */
@OptIn(FormatStringsInDatetimeFormats::class)
val patternFormat = LocalDateTime.Format {
    byUnicodePattern("HH:mm:ss.SSS")
}

/**
 * ANSI color codes for console log output.
 */
private enum class AnsiColor(val code: String) {
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    GRAY("\u001B[90m"),
}

/**
 * Single-character severity prefix for log entries.
 */
private val Severity.prefix: String
    get() = when (this) {
        Severity.Verbose -> "V"
        Severity.Debug -> "D"
        Severity.Info -> "I"
        Severity.Warn -> "W"
        Severity.Error -> "E"
        Severity.Assert -> "A"
    }

/**
 * ANSI color for each severity level.
 */
private val Severity.color: AnsiColor
    get() = when (this) {
        Severity.Verbose -> AnsiColor.GRAY
        Severity.Debug -> AnsiColor.GREEN
        Severity.Info -> AnsiColor.CYAN
        Severity.Warn -> AnsiColor.YELLOW
        Severity.Error -> AnsiColor.RED
        Severity.Assert -> AnsiColor.MAGENTA
    }

/**
 * Console log writer with ANSI colors and timestamps.
 */
private object ColoredConsoleLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).format(patternFormat)
        val cleanMessage = message.lines().joinToString(" ").trim()
        val tagString = if (tag.isNotEmpty()) "${AnsiColor.BLUE.code}[$tag]${AnsiColor.RESET.code} " else ""
        val coloredPrefix = "${severity.color.code}[${severity.prefix}]${AnsiColor.RESET.code}"
        synchronizedPrint {
            println("$time $coloredPrefix $tagString$cleanMessage")
            throwable?.printStackTrace()
        }
    }
}

/**
 * Koin module that configures Kermit logging with ANSI-colored console output.
 */
val loggingModule: Module = module {
    Logger.setMinSeverity(Severity.Verbose)
    Logger.setLogWriters(ColoredConsoleLogWriter)
    single<Logger> { Logger }
}
