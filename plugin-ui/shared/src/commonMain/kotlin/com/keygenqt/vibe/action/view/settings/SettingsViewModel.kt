/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.models.SettingsStatusModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the settings screen.
 */
class SettingsViewModel(
    env: Environment,
    view: PlatformView,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    private val _status = MutableStateFlow(fakeStatus())
    val status: StateFlow<SettingsStatusModel> = _status.asStateFlow()

    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
    }

    /**
     * @todo wire up to `vibe-action clean` via the CLI bridge — logging only for now.
     */
    fun cleanCache() {
        logger.d { "cleanCache" }
    }

    /**
     * @todo wire up to SysBridge.openFile with the real config path — logging only for now.
     */
    fun openConfigFile() {
        logger.d { "openConfigFile" }
    }

    private fun fakeStatus() = SettingsStatusModel(
        actionsCount = 20,
        cliVersion = "v0.1.1",
        configVersion = "v0.0.3",
    )
}
