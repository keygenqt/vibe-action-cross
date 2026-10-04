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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.LocalPluginKoin
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ErrorStateView
import com.keygenqt.vibe.action.components.LoadingLottieAnimation
import com.keygenqt.vibe.action.components.NotificationHandler
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.VersionBannerState
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformString
import com.keygenqt.vibe.action.theme.ColorsApp
import com.keygenqt.vibe.action.view.main.components.ActionsList
import com.keygenqt.vibe.action.view.main.components.MainHeaderActions
import com.keygenqt.vibe.action.view.main.components.VersionBanner

/**
 * Root Actions screen — list of runnable flows, entry point of the plugin.
 */
@Composable
fun MainView(
    viewModel: MainViewModel = LocalPluginKoin.current.get<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    val env = ViewEnvironment.current
    val isLoading by viewModel.isLoading.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val expandedActionId by viewModel.expandedActionId.collectAsState()
    val runningActionId by viewModel.runningActionId.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val showDescriptions by viewModel.showDescriptions.collectAsState()
    val actionToDelete by viewModel.actionToDelete.collectAsState()
    val errorLoad by viewModel.errorLoad.collectAsState()
    val versionBanner by viewModel.versionBanner.collectAsState()
    val expandedGroups by viewModel.expandedGroups.collectAsState()

    NotificationHandler(
        notification = notification,
        onClear = viewModel::clearNotification,
    )

    DeleteActionDialogHandler(
        actionToDelete = actionToDelete,
        onConfirm = viewModel::confirmDeleteAction,
        onCancel = viewModel::cancelDeleteAction,
    )

    ScreenScaffold(
        scrollable = !isLoading && !errorLoad,
        title = env.bridge.res.string(PlatformString.MainTitle),
        titleIcon = {
            Icon(
                painter = env.bridge.res.icon(PlatformIcon.AppIcon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = ColorsApp.accent,
            )
        },
        actions = {
            MainHeaderActions(
                onNavigateToAbout = onNavigateToAbout,
                onNavigateToSettings = onNavigateToSettings,
                onCreate = viewModel::createAction,
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
            if (errorLoad) {
                ErrorStateView()
            } else {
                VersionBanner(versionBanner)
                ActionsList(
                    actions = actions,
                    expandedActionId = expandedActionId,
                    runningActionId = runningActionId,
                    showDescriptions = showDescriptions,
                    showVersionBanner = versionBanner != VersionBannerState.None,
                    expandedGroups = expandedGroups,
                    onToggleGroup = viewModel::toggleGroup,
                    onToggleExpanded = viewModel::toggleExpanded,
                    onRun = viewModel::runAction,
                    onEdit = viewModel::editAction,
                    onCancel = viewModel::cancelAction,
                    onDelete = viewModel::requestDeleteAction,
                    onToggleStar = viewModel::onToggleStar,
                )
            }
        }
    }
}

/**
 * Observes action deletion requests and triggers a native confirmation dialog.
 */
@Composable
fun DeleteActionDialogHandler(
    actionToDelete: ActionModel?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val env = ViewEnvironment.current

    // Extract strings outside LaunchedEffect
    val title = if (actionToDelete != null) {
        env.bridge.res.string(PlatformString.DeleteActionTitle)
    } else {
        null
    }

    val message = if (actionToDelete != null) {
        env.bridge.res.string(PlatformString.DeleteActionMessage, actionToDelete.name)
    } else {
        null
    }

    LaunchedEffect(actionToDelete, title, message) {
        if (actionToDelete != null && !title.isNullOrEmpty() && !message.isNullOrEmpty()) {
            env.bridge.sys.showConfirmDialog?.invoke(
                title,
                message,
            ) { confirmed ->
                if (confirmed) onConfirm() else onCancel()
            }
        }
    }
}
