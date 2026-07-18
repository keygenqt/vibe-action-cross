package com.keygenqt.vibe.action.view.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.Components
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformImage
import com.keygenqt.vibe.action.resources.PlatformString
import org.koin.compose.koinInject

@Composable
fun MainView(
    viewModel: MainViewModel = koinInject<MainViewModel>(),
    onNavigateToSettings: () -> Unit = {}
) {
    val env = ViewEnvironment.current
    var showFallbackDialog by remember { mutableStateOf(false) }

    fun requestNavigateConfirmation() {
        val nativeConfirm = env.bridge.sys.showConfirmDialog
        if (nativeConfirm != null) {
            nativeConfirm(
                "Navigate to Settings?",
                "You are about to leave the main screen and open the Settings page."
            ) { confirmed ->
                if (confirmed) onNavigateToSettings()
            }
        } else {
            showFallbackDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = env.bridge.res.icon(PlatformIcon.Sidebar),
                    contentDescription = "Aurora Icon",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
                Components.Text(
                    env.bridge.res.string(PlatformString.SidebarTitle),
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = env.bridge.res.image(PlatformImage.Preview),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3BEA62), Color(0xFF3C99CC), Color(0xFF6B57FF))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Components.Text(
                "What this plugin does",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.widthIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Components.Text(
                    "This screen is rendered from a single Kotlin Multiplatform " +
                            "codebase using Compose Multiplatform — one UI, four targets: " +
                            "an IntelliJ Platform plugin, a VS Code extension, a JVM " +
                            "desktop app, and a browser build compiled to Kotlin/JS.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Components.Text(
                    "Platform differences are resolved through expect/actual bridges " +
                            "instead of branching logic: icons, images, and strings go " +
                            "through a shared ResBridge, while native look-and-feel comes " +
                            "from Jewel inside the IDE and falls back to Material3 " +
                            "everywhere else — same call site, different renderer underneath.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Components.Text(
                    "Inside the plugin, this exact Composable is hosted in a Swing " +
                            "ComposePanel bridged into the IntelliJ tool window; on the web " +
                            "it's the same tree served through a VS Code webview with a " +
                            "remapped resource base. Koin wires the dependencies, and " +
                            "Compose handles the rest — no per-platform UI code required.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Components.Button(onClick = { requestNavigateConfirmation() }) {
                Components.Text("Go to Settings")
            }
        }
    }

    if (showFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showFallbackDialog = false },
            title = { Components.Text("Navigate to Settings?") },
            text = { Components.Text("You are about to leave the main screen and open the Settings page.") },
            confirmButton = {
                Components.Button(onClick = {
                    showFallbackDialog = false
                    onNavigateToSettings()
                }) { Components.Text("Go to Settings") }
            },
            dismissButton = {
                Components.Button(onClick = { showFallbackDialog = false }) { Components.Text("Cancel") }
            }
        )
    }
}
