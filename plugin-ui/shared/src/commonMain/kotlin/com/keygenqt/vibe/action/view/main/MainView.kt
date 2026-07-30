/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.LoadingLottieAnimation
import com.keygenqt.vibe.action.components.NotificationHandler
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.view.main.components.ActionsList
import com.keygenqt.vibe.action.view.main.components.ActionsToolbar
import com.keygenqt.vibe.action.view.main.components.MainHeaderActions
import org.koin.compose.koinInject

/**
 * Root Actions screen — list of runnable flows, entry point of the plugin.
 */
@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    val env = ViewEnvironment.current
    val isLoading by viewModel.isLoading.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val expandedActionId by viewModel.expandedActionId.collectAsState()
    val runningActionId by viewModel.runningActionId.collectAsState()
    val notification by viewModel.notification.collectAsState()

    NotificationHandler(
        notification = notification,
        onClear = viewModel::clearNotification,
    )

    ScreenScaffold(
        scrollable = !isLoading,
        title = env.bridge.res.string(PlatformString.MainTitle),
        titleIcon = {
            Icon(
                painter = env.bridge.res.icon(PlatformIcon.AppIcon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        actions = {
            MainHeaderActions(
                onNavigateToAbout = onNavigateToAbout,
                onNavigateToSettings = onNavigateToSettings,
                onRefresh = viewModel::refresh,
                isRefreshing = isLoading,
            )
        },
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingLottieAnimation(modifier = Modifier.fillMaxSize())
            }
        } else {
            ActionsToolbar(count = actions.size)
            ActionsList(
                actions = actions,
                expandedActionId = expandedActionId,
                runningActionId = runningActionId,
                onToggleExpanded = viewModel::toggleExpanded,
                onRun = viewModel::runAction,
                onCancel = viewModel::cancelAction,
                onDelete = viewModel::deleteAction,
            )
        }
    }
}
