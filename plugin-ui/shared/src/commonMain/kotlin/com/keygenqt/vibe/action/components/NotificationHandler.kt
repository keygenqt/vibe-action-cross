package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.models.NotificationModel

/**
 * Observes notification state from ViewModel, resolves localized strings
 * in a composable context, and triggers the platform notification.
 */
@Composable
fun NotificationHandler(
    notification: NotificationModel?,
    onClear: () -> Unit,
) {
    val env = ViewEnvironment.current

    // Extract keys and params to avoid calling @Composable inside let{}
    val titleKey = notification?.title?.key
    val titleParams = notification?.title?.params.orEmpty().toTypedArray()
    val messageKey = notification?.message?.key
    val messageParams = notification?.message?.params.orEmpty().toTypedArray()

    val notifTitle = if (titleKey != null) {
        env.bridge.res.string(titleKey, *titleParams)
    } else null

    val notifMessage = if (messageKey != null) {
        env.bridge.res.string(messageKey, *messageParams)
    } else null

    // Restart effect if a new notification arrives OR if strings finish loading
    LaunchedEffect(notification, notifTitle, notifMessage) {
        if (notification != null && !notifTitle.isNullOrEmpty() && !notifMessage.isNullOrEmpty()) {
            env.bridge.sys.showNotification?.invoke(notifTitle, notifMessage)
            onClear()
        }
    }
}
