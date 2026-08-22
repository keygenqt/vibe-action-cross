/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keygenqt.vibe.action.base.Constants
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.components.Components
import com.keygenqt.vibe.action.models.VersionBannerState
import com.keygenqt.vibe.action.resources.PlatformString

/**
 * Version-sync banner. Renders nothing for [VersionBannerState.None];
 * otherwise shows a short message with a button that opens the matching
 * docs page in the system browser.
 */
@Composable
fun VersionBanner(state: VersionBannerState) {
    val env = ViewEnvironment.current
    when (state) {
        VersionBannerState.None -> Unit
        VersionBannerState.UpdatePlugin -> VersionBannerContent(
            title = env.bridge.res.string(PlatformString.UpdatePluginTitle),
            message = env.bridge.res.string(PlatformString.UpdatePluginDesc),
            url = Constants.URL_DOCS_PLUGIN,
        )

        VersionBannerState.UpdateCli -> VersionBannerContent(
            title = env.bridge.res.string(PlatformString.UpdateAppCliTitle),
            message = env.bridge.res.string(PlatformString.UpdateAppCliDesc),
            url = Constants.URL_DOCS_CLI,
        )
    }
}

/**
 * Shared layout for both banners — title, message, and a button that
 * opens [url] via the platform bridge.
 */
@Composable
private fun VersionBannerContent(
    title: String,
    message: String,
    url: String,
) {
    val env = ViewEnvironment.current
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Components.Button(
                onClick = { env.bridge.sys.openUrl?.invoke(url) },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = env.bridge.res.string(PlatformString.BtnOpenDocs),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}
