package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.resources.PlatformString

/**
 * Top-right action bar icons: jump to History, About, and Settings.
 */
@Composable
fun MainHeaderActions(
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val env = ViewEnvironment.current

    IconButton(onClick = onNavigateToHistory, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = env.bridge.res.string(PlatformString.HistoryTitle),
            modifier = Modifier.size(16.dp),
        )
    }
    IconButton(onClick = onNavigateToAbout, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = env.bridge.res.string(PlatformString.AboutTitle),
            modifier = Modifier.size(16.dp),
        )
    }
    IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = env.bridge.res.string(PlatformString.SettingsTitle),
            modifier = Modifier.size(16.dp),
        )
    }
}
