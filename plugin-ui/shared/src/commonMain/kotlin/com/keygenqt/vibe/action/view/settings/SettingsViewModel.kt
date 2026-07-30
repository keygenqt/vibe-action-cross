/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.settings

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.AppEvent
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.base.EventBus
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.ToolingRepository
import com.keygenqt.vibe.action.models.NotificationModel
import com.keygenqt.vibe.action.models.SettingsStatusModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen.
 */
class SettingsViewModel(
    env: Environment,
    view: PlatformView,
    private val toolingRepository: ToolingRepository,
    private val eventBus: EventBus,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    /**
     * Holds a one-shot notification payload to be shown by the UI.
     * UI is responsible for calling [clearNotification] after displaying.
     */
    private val _notification = MutableStateFlow<NotificationModel?>(null)
    val notification: StateFlow<NotificationModel?> = _notification.asStateFlow()

    /**
     * Holds the current settings status.
     */
    private val _status = MutableStateFlow<SettingsStatusModel?>(null)
    val status: StateFlow<SettingsStatusModel?> = _status.asStateFlow()

    /**
     * Indicates whether a data loading operation is currently in progress.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    /**
     * Loads the status from the CLI and maps it to the UI model.
     */
    private fun loadData() {
        _isLoading.value = !toolingRepository.isCached()
        viewModelScope.launch {
            try {
                val config = toolingRepository.getStatus()
                logger.d { "Setting status: $config" }
                if (config != null) {
                    _status.value = SettingsStatusModel(
                        actionsCount = config.actions,
                        cliVersion = config.version,
                        configVersion = config.config,
                        actionsPath = config.actionsPath,
                        configPath = config.configPath,
                        cachePath = config.cachePath,
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cleans the cache and shows a notification.
     */
    fun cleanCache() {
        viewModelScope.launch {
            try {
                toolingRepository.cleanCache()
                _notification.value = NotificationModel.cacheCleaned()
                eventBus.emit(AppEvent.CacheCleared)
            } catch (e: Exception) {
                _notification.value = NotificationModel.cacheCleanFailed(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches the config path and opens it in the native editor.
     */
    fun openConfigFile() {
        viewModelScope.launch {
            val path = _status.value?.configPath ?: return@launch
            env.bridge.sys.openFile?.invoke(path)
        }
    }

    /**
     * Clears the current notification state.
     */
    fun clearNotification() {
        _notification.value = null
    }
}
