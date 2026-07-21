package com.keygenqt.vibe.action.view.main.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.resources.PlatformIcon

/**
 * App bolt icon shown next to the "Actions" title.
 */
@Composable
fun MainTitleIcon(env: Environment) {
    Icon(
        painter = env.bridge.res.icon(PlatformIcon.AppIcon),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
    )
}
