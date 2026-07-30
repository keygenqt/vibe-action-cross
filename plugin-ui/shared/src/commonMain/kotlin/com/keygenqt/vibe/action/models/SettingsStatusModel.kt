/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

/**
 * Snapshot of vibe-action CLI status, shown on the Settings screen.
 */
data class SettingsStatusModel(
    val actionsCount: Int,
    val cliVersion: String,
    val configVersion: String,
    val actionsPath: String,
    val configPath: String,
    val cachePath: String,
)
