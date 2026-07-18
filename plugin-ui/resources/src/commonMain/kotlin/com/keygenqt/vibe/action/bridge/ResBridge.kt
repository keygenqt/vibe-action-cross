package com.keygenqt.vibe.action.bridge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.keygenqt.vibe.action.resources.PlatformIcon
import com.keygenqt.vibe.action.resources.PlatformImage
import com.keygenqt.vibe.action.resources.PlatformString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import vibe_action_cross.plugin_ui.resources.generated.resources.Res
import vibe_action_cross.plugin_ui.resources.generated.resources.preview
import vibe_action_cross.plugin_ui.resources.generated.resources.sidebar
import vibe_action_cross.plugin_ui.resources.generated.resources.sidebar_title

/**
 * Compose Multiplatform resource bridge — loads resources via compose.components.resources.
 */
class ComposeResBridge : ResBridge {
    /**
     * Returns a platform icon from compose resources.
     */
    @Composable
    override fun icon(icon: PlatformIcon, size: Int): Painter {
        return when (icon) {
            PlatformIcon.Sidebar -> painterResource(Res.drawable.sidebar)
        }
    }

    /**
     * Returns a platform image from compose resources.
     */
    @Composable
    override fun image(image: PlatformImage, width: Int, height: Int): Painter {
        return when (image) {
            PlatformImage.Preview -> painterResource(Res.drawable.preview)
        }
    }

    /**
     * Returns a localized string from compose resources.
     */
    @Composable
    override fun string(key: PlatformString, vararg params: Any?): String {
        return when (key) {
            PlatformString.SidebarTitle -> stringResource(Res.string.sidebar_title)
        }
    }
}
