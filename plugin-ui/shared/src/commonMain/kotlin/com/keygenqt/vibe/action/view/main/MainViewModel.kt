/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.AppEvent
import com.keygenqt.vibe.action.base.BaseViewModel
import com.keygenqt.vibe.action.base.EventBus
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.ActionRepository
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.NotificationModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the main screen. Manages the list of actions, their
 * expansion state, loading/error indicators, and action execution.
 */
class MainViewModel(
    env: Environment,
    view: PlatformView,
    private val actionRepository: ActionRepository,
    private val eventBus: EventBus,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    /**
     * Monotonic sequence of runAction invocations. Guards late continuations
     * of a superseded run: unlike _runningActionId, it also distinguishes a
     * restart of the *same* action (A canceled → A started again), where the
     * id matches but the old job must stay silent.
     * Mutated only on the viewModelScope dispatcher (Main).
     */
    private var runSeq = 0

    /**
     * Holds a one-shot notification payload to be shown by the UI.
     * UI is responsible for calling [clearNotification] after displaying.
     */
    private val _notification = MutableStateFlow<NotificationModel?>(null)
    val notification: StateFlow<NotificationModel?> = _notification.asStateFlow()

    /**
     * Holds the current list of actions displayed in the UI.
     */
    private val _actions = MutableStateFlow<List<ActionModel>>(emptyList())
    val actions: StateFlow<List<ActionModel>> = _actions.asStateFlow()

    /**
     * Stores the id of the action whose detail section is expanded, or null if none.
     */
    private val _expandedActionId = MutableStateFlow<String?>(null)
    val expandedActionId: StateFlow<String?> = _expandedActionId.asStateFlow()

    /**
     * Indicates whether a data loading operation is currently in progress.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Stores the id of the action currently being executed, or null if none.
     */
    private val _runningActionId = MutableStateFlow<String?>(null)
    val runningActionId: StateFlow<String?> = _runningActionId.asStateFlow()

    /**
     * Serializes loadData() calls — init and refresh() must not overlap.
     */
    private val loadMutex = Mutex()

    /**
     * Holds the Job reference for the latest data load coroutine.
     */
    private var loadJob: Job? = null

    /**
     * Holds the Job reference for the currently running action execution.
     */
    private var runJob: Job? = null

    init {
        loadData(false)
        listenToEvents()
    }

    /**
     * Reloads the action list. Cancels any in-progress load and
     * starts a new one with a small artificial delay.
     */
    fun refresh() {
        loadJob?.cancel()
        loadData(true)
    }

    /**
     * Listens for global application events from the EventBus.
     * Used to react to cross-ViewModel signals, such as refreshing the
     * action list when the cache is cleared in the Settings screen.
     */
    private fun listenToEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    AppEvent.CacheCleared -> {
                        logger.d { "Cache cleared event received, refreshing actions." }
                        refresh()
                    }
                }
            }
        }
    }

    /**
     * Loads the action list from the repository.
     * Serialized by [loadMutex] to prevent concurrent loads.
     */
    private fun loadData(showLoader: Boolean) {
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                if (showLoader) {
                    _isLoading.value = true
                    delay(1000.milliseconds)
                }
                try {
                    _actions.value = actionRepository.loadActions()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.e(e) { "Load error" }
                    _notification.value = NotificationModel.loadError(e.message ?: "Unknown error")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Toggles the expanded state of the action with the given id.
     */
    fun toggleExpanded(id: String) {
        _expandedActionId.update { current -> if (current == id) null else id }
    }

    /**
     * Runs the action with the specified id, updates the text, and pastes it back.
     * Cancels any previously running action before starting.
     *
     * Cancellation kills the previous CLI process via the CliProcess handle
     * (see CommandProvider.execute) — no 'stop' command is needed; the new
     * process's Rust RunGuard backstops the shutdown.
     */
    fun runAction(id: String) {
        val action = _actions.value.find { it.id == id } ?: return

        // Supersede the previous run. Its catch/finally below are guarded by
        // runSeq, so they won't clobber this run's state or notifications.
        runJob?.cancel()
        val seq = ++runSeq

        _runningActionId.value = id
        logger.d { "Run action: $action" }

        runJob = viewModelScope.launch {
            try {
                actionRepository.executeAction(
                    action = action,
                    // isActive: a canceled job can still reach these in the
                    // non-suspending tail of executeAction after cancellation.
                    onSuccess = { if (isActive) _notification.value = NotificationModel.actionCompleted(action.name) },
                    onEmpty = { if (isActive) _notification.value = NotificationModel.actionEmptyOutput(action.name) },
                )
            } catch (e: CancellationException) {
                // Silent when superseded — the newer run owns the UI now.
                if (runSeq == seq) {
                    _notification.value = NotificationModel.actionCancelled(action.name)
                }
                throw e
            } catch (e: Exception) {
                if (runSeq == seq) {
                    _notification.value = NotificationModel.actionFailed(action.name)
                } else {
                    logger.w(e) { "Superseded action ${action.name} failed" }
                }
            } finally {
                // A newer run may already have claimed the slot — don't clear it.
                // (The original `= null` wiped the NEW action's running state
                // when the old job unwound after the new one had started.)
                if (runSeq == seq) {
                    _runningActionId.value = null
                }
            }
        }
    }

    /**
     * Cancels the currently running action, if any.
     * The coroutine unwinds → invokeOnCancellation kills the CLI process
     * via CliProcess.cancel, and the "canceled" notification is posted
     * from runAction's catch (runSeq is unchanged here, so the guard passes).
     */
    fun cancelAction() {
        runJob?.cancel()
    }

    /**
     * Clears the current notification state.
     */
    fun clearNotification() {
        _notification.value = null
    }

    /**
     * Deletes the custom action with the given id (only custom actions are allowed).
     */
    fun deleteAction(id: String) {
        val action = _actions.value.find { it.id == id } ?: return
        if (!action.isCustom) {
            logger.w { "Нельзя удалить встроенный экшен: $id" }
            return
        }
        logger.d { "Удаление экшена: $id" }
        // TODO: делегировать удаление в ActionRepository
    }
}
