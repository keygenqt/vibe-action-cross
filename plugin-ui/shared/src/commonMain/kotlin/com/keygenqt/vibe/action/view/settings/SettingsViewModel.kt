package com.keygenqt.vibe.action.view.settings

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView

/**
 * ViewModel for the settings screen.
 */
class SettingsViewModel(
    env: Environment,
    view: PlatformView,
    logger: Logger
) : BaseViewModel(env, view) {
    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
    }
}
