/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Top-right action bar icons: jump to History, About, and Settings.
 */
@Composable
fun MainHeaderActions(
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    IconButton(
        onClick = onCreate,
        modifier = Modifier.size(24.dp),
        enabled = !isRefreshing,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }

    IconButton(
        onClick = onNavigateToSettings,
        modifier = Modifier.size(24.dp),
        enabled = !isRefreshing,
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }

    IconButton(
        onClick = onNavigateToAbout,
        modifier = Modifier.size(24.dp),
        enabled = !isRefreshing,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }

    IconButton(
        onClick = onRefresh,
        enabled = !isRefreshing,
        modifier = Modifier.size(24.dp),
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
