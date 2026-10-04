/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    override fun string(key: PlatformString, vararg params: Any?): String = remember(key, *params) {
        val bundleKey = when (key) {
            // Root Actions screen — tool window stripe label
            PlatformString.SidebarTitle -> "toolwindow.stripe.Sidebar"

            // Actions screen
            PlatformString.MainTitle -> "main.title"

            // Action delete dialog
            PlatformString.DeleteActionTitle -> "action.delete.title"
            PlatformString.DeleteActionMessage -> "action.delete.message"

            // About screen
            PlatformString.AboutTitle -> "about.title"
            PlatformString.AboutDescription1 -> "about.description1"
            PlatformString.AboutDescription2 -> "about.description2"
            PlatformString.AboutDescription3 -> "about.description3"
            PlatformString.AboutRequirement -> "about.requirement"

            // Settings screen
            PlatformString.SettingsTitle -> "settings.title"
            PlatformString.SettingsOpenConfigButton -> "settings.open_config_button"
            PlatformString.SettingsCleanCacheButton -> "settings.clean_cache_button"
            PlatformString.SettingsStatusActionsLabel -> "settings.status.actions_label"
            PlatformString.SettingsStatusVersionLabel -> "settings.status.version_label"
            PlatformString.SettingsStatusConfigLabel -> "settings.status.config_label"
            PlatformString.SettingsConfigDescription -> "settings.config.description"
            PlatformString.SettingsCacheDescription -> "settings.cache.description"
            PlatformString.SettingsStatusDescription -> "settings.status.description"

            // Common
            PlatformString.CommonRefresh -> "common.refresh"
            PlatformString.CommonErrorLoad -> "common.error.load"
            PlatformString.Custom -> "common.custom"

            // Action notifications
            PlatformString.NotifLoadErrorTitle -> "notif.load.error.title"
            PlatformString.NotifLoadErrorDesc -> "notif.load.error.desc"
            PlatformString.NotifActionCompletedTitle -> "notif.action.completed.title"
            PlatformString.NotifActionCompletedDesc -> "notif.action.completed.desc"
            PlatformString.NotifEmptyOutputDesc -> "notif.action.emptyOutput.desc"
            PlatformString.NotifActionCancelledTitle -> "notif.action.cancelled.title"
            PlatformString.NotifActionCancelledDesc -> "notif.action.cancelled.desc"
            PlatformString.NotifActionFailedTitle -> "notif.action.failed.title"
            PlatformString.NotifActionFailedDesc -> "notif.action.failed.desc"
            PlatformString.NotifActionDeleteSuccessTitle -> "notif.action.delete.success.title"
            PlatformString.NotifActionDeleteSuccessDesc -> "notif.action.delete.success.desc"
            PlatformString.NotifActionDeleteFailedTitle -> "notif.action.delete.failed.title"
            PlatformString.NotifActionDeleteFailedDesc -> "notif.action.delete.failed.desc"

            // Cache Notifications
            PlatformString.NotifCacheCleanedTitle -> "notif.cache.cleaned.title"
            PlatformString.NotifCacheCleanedDesc -> "notif.cache.cleaned.desc"
            PlatformString.NotifCacheCleanFailedTitle -> "notif.cache.clean.failed.title"
            PlatformString.NotifCacheCleanFailedDesc -> "notif.cache.clean.failed.desc"

            // Appearance Settings
            PlatformString.SettingsAppearanceTitle -> "settings.appearance.title"
            PlatformString.SettingsAppearanceDescription -> "settings.appearance.description"
            PlatformString.SettingsShowDescriptionsLabel -> "settings.show.descriptions.label"

            // Actions Group
            PlatformString.ActionGroupFavorite -> "action.group.favorite"
            PlatformString.ActionGroupFavoriteAbout -> "action.group.favorite_about"
            PlatformString.ActionGroupCustom -> "action.group.custom"
            PlatformString.ActionGroupCustomAbout -> "action.group.custom_about"
            PlatformString.ActionGroupDefault -> "action.group.default"
            PlatformString.ActionGroupDefaultAbout -> "action.group.default_about"

            // Version Banner
            PlatformString.BtnOpenDocs -> "version.banner.open_docs"
            PlatformString.UpdatePluginTitle -> "version.banner.update_plugin.title"
            PlatformString.UpdatePluginDesc -> "version.banner.update_plugin.desc"
            PlatformString.UpdateAppCliTitle -> "version.banner.update_cli.title"
            PlatformString.UpdateAppCliDesc -> "version.banner.update_cli.desc"
        }
        MessageBundle.message(bundleKey, *params)
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
