package com.keygenqt.vibe.action.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.keygenqt.vibe.action.bridge.Platform
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import org.jetbrains.jewel.ui.component.DefaultButton as JButton
import org.jetbrains.jewel.ui.component.Text as JText

/**
 * JVM target — provides Jewel components for IntelliJ plugin, falls back to Material3 for Desktop.
 */
actual object PlatformComponents {
    @Composable
    actual fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        content: @Composable () -> Unit
    ): (@Composable () -> Unit)? {
        if (ViewEnvironment.current.platform != Platform.IntellJPlugin) return null
        return {
            JButton(onClick = onClick, modifier = modifier) {
                content()
            }
        }
    }

    @Composable
    actual fun Text(
        text: String,
        modifier: Modifier,
        fontSize: TextUnit,
        color: Color,
        textAlign: TextAlign,
    ): (@Composable () -> Unit)? {
        if (ViewEnvironment.current.platform != Platform.IntellJPlugin) return null
        return { JText(text = text, modifier = modifier, fontSize = fontSize, color = color, textAlign = textAlign) }
    }
}
