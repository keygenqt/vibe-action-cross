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
import vibe_action_cross.plugin_ui.resources.generated.resources.common_refresh
import vibe_action_cross.plugin_ui.resources.generated.resources.history_title
import vibe_action_cross.plugin_ui.resources.generated.resources.main_title
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_cancelled_desc
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_cancelled_title
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_completed_desc
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_completed_title
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_empty_output_desc
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_failed_desc
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_action_failed_title
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_load_error_desc
import vibe_action_cross.plugin_ui.resources.generated.resources.notification_load_error_title
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_cache_description
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_clean_cache_button
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_config_description
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_open_config_button
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_status_actions_label
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_status_config_label
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_status_description
import vibe_action_cross.plugin_ui.resources.generated.resources.settings_status_version_label
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
            // Root Actions screen — tool window stripe label
            PlatformString.SidebarTitle -> stringResource(Res.string.sidebar_title, *values)

            // Actions screen
            PlatformString.MainTitle -> stringResource(Res.string.main_title, *values)

            // About screen
            PlatformString.AboutTitle -> stringResource(Res.string.about_title, *values)
            PlatformString.AboutDescription1 -> stringResource(Res.string.about_description1, *values)
            PlatformString.AboutDescription2 -> stringResource(Res.string.about_description2, *values)
            PlatformString.AboutRequirement -> stringResource(Res.string.about_requirement, *values)

            // History screen
            PlatformString.HistoryTitle -> stringResource(Res.string.history_title, *values)

            // Settings screen
            PlatformString.SettingsTitle -> stringResource(Res.string.settings_title, *values)
            PlatformString.SettingsConfigDescription -> stringResource(Res.string.settings_config_description, *values)
            PlatformString.SettingsOpenConfigButton -> stringResource(Res.string.settings_open_config_button, *values)
            PlatformString.SettingsCacheDescription -> stringResource(Res.string.settings_cache_description, *values)
            PlatformString.SettingsCleanCacheButton -> stringResource(Res.string.settings_clean_cache_button, *values)
            PlatformString.SettingsStatusDescription -> stringResource(Res.string.settings_status_description, *values)
            PlatformString.SettingsStatusActionsLabel -> stringResource(Res.string.settings_status_actions_label, *values)
            PlatformString.SettingsStatusVersionLabel -> stringResource(Res.string.settings_status_version_label, *values)
            PlatformString.SettingsStatusConfigLabel -> stringResource(Res.string.settings_status_config_label, *values)

            // Common
            PlatformString.CommonRefresh -> stringResource(Res.string.common_refresh, *values)

            // Action notifications
            PlatformString.NotifLoadErrorTitle -> stringResource(Res.string.notification_load_error_title, *values)
            PlatformString.NotifLoadErrorDesc -> stringResource(Res.string.notification_load_error_desc, *values)

            PlatformString.NotifActionCompletedTitle -> stringResource(Res.string.notification_action_completed_title, *values)
            PlatformString.NotifActionCompletedDesc -> stringResource(Res.string.notification_action_completed_desc, *values)
            PlatformString.NotifEmptyOutputDesc -> stringResource(Res.string.notification_action_empty_output_desc, *values)
            PlatformString.NotifActionCancelledTitle -> stringResource(Res.string.notification_action_cancelled_title, *values)
            PlatformString.NotifActionCancelledDesc -> stringResource(Res.string.notification_action_cancelled_desc, *values)
            PlatformString.NotifActionFailedTitle -> stringResource(Res.string.notification_action_failed_title, *values)
            PlatformString.NotifActionFailedDesc -> stringResource(Res.string.notification_action_failed_desc, *values)
        }
    }
}
