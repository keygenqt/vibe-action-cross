/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.view.main.components.ActionsList
import com.keygenqt.vibe.action.view.main.components.ActionsToolbar
import com.keygenqt.vibe.action.view.main.components.MainHeaderActions
import com.keygenqt.vibe.action.view.main.components.MainTitleIcon
import org.koin.compose.koinInject

/**
 * Root Actions screen — list of runnable flows, entry point of the plugin.
 */
@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    val env = ViewEnvironment.current
    val actions by viewModel.actions.collectAsState()
    val expandedActionId by viewModel.expandedActionId.collectAsState()

    ScreenScaffold(
        title = "Actions",
        titleIcon = { MainTitleIcon(env) },
        actions = {
            MainHeaderActions(
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToAbout = onNavigateToAbout,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
    ) {
        ActionsToolbar(count = actions.size)
        ActionsList(
            actions = actions,
            expandedActionId = expandedActionId,
            onToggleExpanded = viewModel::toggleExpanded,
            onRun = viewModel::runAction,
            onDelete = viewModel::deleteAction,
        )
    }
}













