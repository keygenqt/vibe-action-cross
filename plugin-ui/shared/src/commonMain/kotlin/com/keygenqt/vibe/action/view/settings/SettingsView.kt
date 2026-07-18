package com.keygenqt.vibe.action.view.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.components.Components
import org.koin.compose.koinInject

@Composable
fun SettingsView(
    viewModel: SettingsViewModel = koinInject<SettingsViewModel>(),
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Components.Text("Settings View", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Components.Text(
                    "This is a placeholder screen — in a real plugin it's where " +
                            "you'd expose configuration, preferences, or account settings " +
                            "specific to whatever the plugin actually does.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Components.Text(
                    "What matters here isn't the content, but that you just got to " +
                            "this screen at all: the same navigation graph, built with " +
                            "Navigation 3, drives every one of the four targets.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Components.Text(
                    "Back-stack, transitions, and the Back button below all behave " +
                            "identically whether you're inside the IDE, VS Code, the " +
                            "desktop app, or the browser — one navigation graph, not four.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Components.Button(onClick = onBack) {
                Components.Text("Go to Back")
            }
        }
    }
}
