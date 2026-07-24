/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

/**
 * A single past flow run shown in the History list.
 */
data class HistoryItemModel(
    val id: String,
    val actionName: String,
    val timestamp: String,
    val isSuccess: Boolean,
)
