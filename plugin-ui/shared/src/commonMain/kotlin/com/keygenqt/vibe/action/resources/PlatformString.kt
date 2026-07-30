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

    // Cache notifications – title and description keys
    NotifCacheCleanedTitle,
    NotifCacheCleanedDesc,
    NotifCacheCleanFailedTitle,
    NotifCacheCleanFailedDesc,
}
