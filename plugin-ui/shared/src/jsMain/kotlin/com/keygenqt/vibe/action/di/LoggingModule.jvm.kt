/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.di

/**
 * No-op on JS: the runtime is single-threaded, so there's no interleaving to guard against.
 */
internal actual fun synchronizedPrint(block: () -> Unit) {
    block()
}
