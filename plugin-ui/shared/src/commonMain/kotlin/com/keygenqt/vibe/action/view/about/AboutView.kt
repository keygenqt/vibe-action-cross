/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.base.Constants
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.ScreenScaffold
import com.keygenqt.vibe.action.resources.PlatformImage
import com.keygenqt.vibe.action.resources.PlatformString
import org.koin.compose.koinInject

@Composable
fun AboutView(
    viewModel: AboutViewModel = koinInject<AboutViewModel>(),
    onBack: () -> Unit = {},
) {
    val env = ViewEnvironment.current

    ScreenScaffold(
        title = env.bridge.res.string(PlatformString.AboutTitle),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = env.bridge.res.image(PlatformImage.Architecture),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null,
                    modifier = Modifier
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6366f1), Color(0xFFa78bfa), Color(0xFF10b981)),
                            ),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 18.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = env.bridge.res.string(PlatformString.AboutDescription1),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Text(
                text = env.bridge.res.string(PlatformString.AboutDescription2),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Text(
                text = env.bridge.res.string(PlatformString.AboutRequirement),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v${Constants.VERSION} · ${Constants.AUTHOR}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
