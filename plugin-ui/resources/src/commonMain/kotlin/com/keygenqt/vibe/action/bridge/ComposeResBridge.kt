/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformImage
import com.keygenqt.vibe.action.resources.PlatformString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vibe_action_cross.plugin_ui.resources.generated.resources.Res
import vibe_action_cross.plugin_ui.resources.generated.resources.about_description1
import vibe_action_cross.plugin_ui.resources.generated.resources.about_description2
import vibe_action_cross.plugin_ui.resources.generated.resources.about_requirement
import vibe_action_cross.plugin_ui.resources.generated.resources.about_title
import vibe_action_cross.plugin_ui.resources.generated.resources.architecture_diagram
import vibe_action_cross.plugin_ui.resources.generated.resources.app_icon
import vibe_action_cross.plugin_ui.resources.generated.resources.history_title
import vibe_action_cross.plugin_ui.resources.generated.resources.main_title
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_title
import vibe_action_cross.plugin_ui.resources.generated.resources.sidebar_title

/**
 * Compose Multiplatform resource bridge — loads resources via compose.components.resources.
 */
class ComposeResBridge : ResBridge {
    /**
     * Returns a platform icon from compose resources.
     */
    @Composable
    override fun icon(icon: PlatformIcon, size: Int): Painter = when (icon) {
        PlatformIcon.AppIcon -> painterResource(Res.drawable.app_icon)
    }

    /**
     * Returns a platform image from compose resources.
     */
    @Composable
    override fun image(image: PlatformImage, width: Int, height: Int): Painter = when (image) {
        PlatformImage.Architecture -> painterResource(Res.drawable.architecture_diagram)
    }

    /**
     * Returns a localized string from compose resources.
     */
    @Composable
    override fun string(key: PlatformString, vararg params: Any?): String {
        val values = params.filterNotNull().toTypedArray()
        return when (key) {
            PlatformString.SidebarTitle -> stringResource(Res.string.sidebar_title, *values)
            PlatformString.MainTitle -> stringResource(Res.string.main_title, *values)
            PlatformString.AboutTitle -> stringResource(Res.string.about_title, *values)
            PlatformString.AboutDescription1 -> stringResource(Res.string.about_description1, *values)
            PlatformString.AboutDescription2 -> stringResource(Res.string.about_description2, *values)
            PlatformString.AboutRequirement -> stringResource(Res.string.about_requirement, *values)
            PlatformString.HistoryTitle -> stringResource(Res.string.history_title, *values)
            PlatformString.SettingsTitle -> stringResource(Res.string.settings_title, *values)
        }
    }
}
