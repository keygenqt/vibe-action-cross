/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Receiver scope for view composables. Provides restricted access to the platform environment.
 */
class ViewScope internal constructor()

/**
 * Static CompositionLocal to resolve the active runtime Environment in ViewScope composables.
 */
val ViewEnvironment = staticCompositionLocalOf<Environment> {
    error("Environment was not provided — wrap your content with InitPage first.")
}
