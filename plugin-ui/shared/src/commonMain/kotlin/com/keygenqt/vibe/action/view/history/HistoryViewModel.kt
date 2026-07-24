/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.history

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.models.HistoryItemModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the History screen.
 */
class HistoryViewModel(
    env: Environment,
    view: PlatformView,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    private val _items = MutableStateFlow(fakeItems())
    val items: StateFlow<List<HistoryItemModel>> = _items.asStateFlow()

    private val _expandedItemId = MutableStateFlow<String?>(null)
    val expandedItemId: StateFlow<String?> = _expandedItemId.asStateFlow()

    init {
        logger.d { "platform: ${env.platform.name}" }
        logger.d { "view: ${view.name}" }
        logger.d { "bridge: ${bridge::class.simpleName}" }
    }

    fun toggleExpanded(id: String) {
        _expandedItemId.update { current -> if (current == id) null else id }
    }

    /**
     * @todo wire up to real history deletion — logging only for now.
     */
    fun deleteItem(id: String) {
        logger.d { "deleteItem: $id" }
    }

    private fun fakeItems() = listOf(
        HistoryItemModel(id = "run-1", actionName = "Explain", timestamp = "19 Jul 2026, 14:32", isSuccess = true),
        HistoryItemModel(id = "run-2", actionName = "Rewrite tone", timestamp = "19 Jul 2026, 14:18", isSuccess = false),
        HistoryItemModel(id = "run-3", actionName = "Review", timestamp = "18 Jul 2026, 18:05", isSuccess = true),
        HistoryItemModel(id = "run-4", actionName = "Comment", timestamp = "18 Jul 2026, 11:47", isSuccess = true),
        HistoryItemModel(id = "run-5", actionName = "Explain", timestamp = "17 Jul 2026, 09:20", isSuccess = true),
    )
}
