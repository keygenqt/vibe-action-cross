package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformImage
import com.keygenqt.vibe.action.resources.PlatformString

/**
 * Platform-specific resource bridge — provides icons, images, and localized strings.
 */
interface ResBridge {
    /**
     * Returns a platform icon as a Compose Painter.
     */
    @Composable
    fun icon(icon: PlatformIcon, size: Int = 40): Painter

    /**
     * Returns a platform image as a Compose Painter.
     */
    @Composable
    fun image(image: PlatformImage, width: Int = 0, height: Int = 0): Painter

    /**
     * Returns a localized string by key with optional format arguments.
     */
    @Composable
    fun string(key: PlatformString, vararg params: Any?): String
}
