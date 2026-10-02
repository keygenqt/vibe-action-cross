/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

/**
 * Outcome of comparing the installed CLI version against
 * `Constants.SUPPORTED_CLI_VERSION` (the latest supported CLI version).
 * Drives the version-sync banner shown on the main screen.
 */
sealed class VersionBannerState {

    /**
     * Installed CLI `major.minor` matches the supported one — no banner.
     */
    object None : VersionBannerState()

    /**
     * Installed CLI is newer than the plugin supports — prompt to update the plugin.
     */
    object UpdatePlugin : VersionBannerState()

    /**
     * Installed CLI is older than the plugin supports — prompt to update the CLI.
     */
    object UpdateCli : VersionBannerState()

    companion object {
        /**
         * Compares two version strings by `major.minor`.
         * Patch is ignored: this is a compatibility signal, and by semver
         * patch releases must not break the plugin-CLI interface.
         * Returns [VersionBannerState.None] if either version can't be parsed.
         */
        fun compare(
            cliVersion: String,
            expectedVersion: String,
        ): VersionBannerState {
            val (cliMajor, cliMinor) = parseMajorMinor(cliVersion) ?: return None
            val (expMajor, expMinor) = parseMajorMinor(expectedVersion) ?: return None
            val cmp = if (cliMajor != expMajor) cliMajor.compareTo(expMajor) else cliMinor.compareTo(expMinor)
            return when {
                cmp == 0 -> None
                cmp > 0 -> UpdatePlugin
                else -> UpdateCli
            }
        }

        /**
         * Extracts `major` and `minor` from a version string, tolerating a leading
         * `v` (e.g. `"v0.2.5"` → `0 to 2`). Returns null if no `major.minor`
         * prefix is found.
         */
        private fun parseMajorMinor(version: String): Pair<Int, Int>? {
            val match = Regex("""v?(\d+)\.(\d+)""").find(version) ?: return null
            val major = match.groupValues[1].toIntOrNull() ?: return null
            val minor = match.groupValues[2].toIntOrNull() ?: return null
            return major to minor
        }
    }
}
