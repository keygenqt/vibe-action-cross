package com.keygenqt.vibe.action.resources

import com.intellij.DynamicBundle
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

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
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any?): String {
        return instance.getMessage(key, *params)
    }
}
