/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.resources

/**
 * Platform-specific string resource identifiers.
 */
enum class PlatformString {
    // Root Actions screen — tool window stripe label
    SidebarTitle,

    // Actions screen
    MainTitle,

    // Action delete dialog
    DeleteActionTitle,
    DeleteActionMessage,

    // About screen
    AboutTitle,
    AboutDescription1,
    AboutDescription2,
    AboutRequirement,

    // Settings screen
    SettingsTitle,
    SettingsConfigDescription,
    SettingsOpenConfigButton,
    SettingsCacheDescription,
    SettingsCleanCacheButton,
    SettingsStatusDescription,
    SettingsStatusActionsLabel,
    SettingsStatusVersionLabel,
    SettingsStatusConfigLabel,

    // Appearance settings
    SettingsAppearanceTitle,
    SettingsAppearanceDescription,
    SettingsShowDescriptionsLabel,

    // Actions group
    ActionGroupFavorite,
    ActionGroupCustom,
    ActionGroupDefault,

    // Common
    CommonRefresh,

    // Action notifications – title and description keys
    NotifActionCompletedTitle,
    NotifActionCompletedDesc,
    NotifEmptyOutputDesc,
    NotifActionCancelledTitle,
    NotifActionCancelledDesc,
    NotifActionFailedTitle,
    NotifActionFailedDesc,
    NotifLoadErrorTitle,
    NotifLoadErrorDesc,
    NotifActionDeleteSuccessTitle,
    NotifActionDeleteSuccessDesc,
    NotifActionDeleteFailedTitle,
    NotifActionDeleteFailedDesc,

    // Cache notifications – title and description keys
    NotifCacheCleanedTitle,
    NotifCacheCleanedDesc,
    NotifCacheCleanFailedTitle,
    NotifCacheCleanFailedDesc,
}
