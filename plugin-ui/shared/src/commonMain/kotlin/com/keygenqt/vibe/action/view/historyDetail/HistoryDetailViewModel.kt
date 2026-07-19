/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.historyDetail

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView

/**
 * ViewModel for a single history run's detail screen.
 */
class HistoryDetailViewModel(
    env: Environment,
    view: PlatformView,
    logger: Logger,
    runId: String,
) : BaseViewModel(env, view) {
    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
        logger.d { "runId: $runId" }
    }
}
