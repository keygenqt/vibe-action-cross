/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.ImageUtil
import com.keygenqt.vibe.action.resources.*
import java.awt.image.BufferedImage
import javax.swing.Icon

/**
 * IntelliJ plugin implementation of the resource bridge.
 */
class PluginResBridge : ResBridge {
    /**
     * Converts a PlatformIcon to a Compose Painter at the given size.
     */
    @Composable
    override fun icon(icon: PlatformIcon, size: Int): Painter = when (icon) {
        PlatformIcon.AppIcon -> IconBundle.AppIcon.toPainter(size, size)
    }

    /**
     * Converts a PlatformImage to a Compose Painter at the given dimensions.
     */
    @Composable
    override fun image(image: PlatformImage, width: Int, height: Int): Painter = when (image) {
        PlatformImage.Architecture -> ImageBundle.Architecture.toPainter(width, height)
    }

    /**
     * Returns a localized string from the plugin message bundle.
     */
    @Composable
    override fun string(key: PlatformString, vararg params: Any?): String {
        val key = when (key) {
            PlatformString.SidebarTitle -> "toolwindow.stripe.Sidebar"
            PlatformString.MainTitle -> "main.title"
            PlatformString.AboutTitle -> "about.title"
            PlatformString.AboutDescription1 -> "about.description1"
            PlatformString.AboutDescription2 -> "about.description2"
            PlatformString.AboutRequirement -> "about.requirement"
            PlatformString.HistoryTitle -> "history.title"
            PlatformString.SettingsTitle -> "settings.title"
            PlatformString.SettingsOpenConfigButton -> "settings.open_config_button"
            PlatformString.SettingsCleanCacheButton -> "settings.clean_cache_button"
            PlatformString.SettingsStatusActionsLabel -> "settings.status.actions_label"
            PlatformString.SettingsStatusVersionLabel -> "settings.status.version_label"
            PlatformString.SettingsStatusConfigLabel -> "settings.status.config_label"
            PlatformString.SettingsConfigDescription -> "settings.config.description"
            PlatformString.SettingsCacheDescription -> "settings.cache.description"
            PlatformString.SettingsStatusDescription -> "settings.status.description"
            PlatformString.CommonRefresh -> "common.refresh"
        }
        return MessageBundle.message(key, *params)
    }
}

/**
 * Converts an IntelliJ [Icon] to a Compose [Painter].
 *
 * If [width] or [height] is 0, that dimension is computed automatically
 * to preserve the icon's aspect ratio relative to the other, non-zero dimension.
 */
fun Icon.toPainter(width: Int, height: Int): Painter {
    if (width < 0 || height < 0 || iconWidth <= 0 || iconHeight <= 0) {
        return ColorPainter(Color.Transparent)
    }
    val targetWidth = when (width) {
        0 if height == 0 -> iconWidth
        0 -> height * iconWidth / iconHeight
        else -> width
    }
    val targetHeight = when {
        width == 0 && height == 0 -> iconHeight
        height == 0 -> width * iconHeight / iconWidth
        else -> height
    }
    if (targetWidth <= 0 || targetHeight <= 0) {
        return ColorPainter(Color.Transparent)
    }
    return try {
        val image: BufferedImage = ImageUtil.createImage(
            targetWidth,
            targetHeight,
            BufferedImage.TYPE_INT_ARGB,
        )
        val graphics = image.createGraphics()
        try {
            graphics.scale(
                targetWidth.toDouble() / iconWidth,
                targetHeight.toDouble() / iconHeight,
            )
            paintIcon(JBLabel(), graphics, 0, 0)
        } finally {
            graphics.dispose()
        }
        BitmapPainter(image.toComposeImageBitmap())
    } catch (_: Exception) {
        ColorPainter(Color.Transparent)
    }
}
