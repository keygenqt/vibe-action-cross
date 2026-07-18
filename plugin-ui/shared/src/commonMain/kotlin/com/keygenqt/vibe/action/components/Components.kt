package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Button as MButton
import androidx.compose.material3.Text as MText

/**
 * Unified component factory — delegates to Jewel (IntelliJ) or Material3 via expect/actual.
 */
object Components {
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        PlatformComponents.Button(onClick, modifier, content)?.invoke() ?: run {
            MButton(onClick = onClick, modifier = modifier) {
                content()
            }
        }
    }

    @Composable
    fun Text(
        text: String,
        modifier: Modifier = Modifier,
        fontSize: TextUnit = TextUnit.Unspecified,
        color: Color = Color.Unspecified,
        textAlign: TextAlign = TextAlign.Unspecified,
    ) {
        PlatformComponents.Text(text, modifier, fontSize, color, textAlign)?.invoke() ?: run {
            MText(text = text, modifier = modifier, fontSize = fontSize, color = color, textAlign = textAlign)
        }
    }
}
