/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.resources

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.MessageBundle"

/**
 * Localization bundle for plugin messages.
 */
internal object MessageBundle {
    private val instance = DynamicBundle(MessageBundle::class.java, BUNDLE)

    /**
     * Returns the localized message for the given key with optional parameters.
     */
    @JvmStatic
    fun message(
        key:
        @PropertyKey(resourceBundle = BUNDLE)
        String,
        vararg params: Any?,
    ): String = instance.getMessage(key, *params)
}
