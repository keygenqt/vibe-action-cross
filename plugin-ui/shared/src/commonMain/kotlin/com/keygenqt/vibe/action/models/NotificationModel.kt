/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.models

import com.keygenqt.vibe.action.resources.PlatformString

/**
 * Represents a notification to be displayed to the user, containing a title and a message.
 */
data class NotificationModel(
    val title: NotificationItem,
    val message: NotificationItem,
) {
    companion object {
        fun loadError(message: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifLoadErrorTitle),
            message = NotificationItem(PlatformString.NotifLoadErrorDesc, listOf(message)),
        )

        fun actionCompleted(name: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifActionCompletedTitle),
            message = NotificationItem(PlatformString.NotifActionCompletedDesc, listOf(name)),
        )

        fun actionEmptyOutput(name: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifActionCompletedTitle),
            message = NotificationItem(PlatformString.NotifEmptyOutputDesc, listOf(name)),
        )

        fun actionCancelled(name: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifActionCancelledTitle),
            message = NotificationItem(PlatformString.NotifActionCancelledDesc, listOf(name)),
        )

        fun actionFailed(name: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifActionFailedTitle),
            message = NotificationItem(PlatformString.NotifActionFailedDesc, listOf(name)),
        )

        fun cacheCleaned() = NotificationModel(
            title = NotificationItem(PlatformString.NotifCacheCleanedTitle),
            message = NotificationItem(PlatformString.NotifCacheCleanedDesc),
        )

        fun cacheCleanFailed(message: String) = NotificationModel(
            title = NotificationItem(PlatformString.NotifCacheCleanFailedTitle),
            message = NotificationItem(PlatformString.NotifCacheCleanFailedDesc, listOf(message)),
        )
    }
}

/**
 * Describes a single localizable string with optional parameters for the notification.
 */
data class NotificationItem(
    val key: PlatformString,
    val params: List<Any?> = emptyList(),
)
