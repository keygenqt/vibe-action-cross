/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.view.main

import co.touchlab.kermit.Logger
import com.keygenqt.vibe.action.base.*
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.ActionRepository
import com.keygenqt.vibe.action.command.ToolingRepository
import com.keygenqt.vibe.action.models.ActionModel
import com.keygenqt.vibe.action.models.NotificationModel
import com.keygenqt.vibe.action.models.VersionBannerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    private val toolingRepository: ToolingRepository,
    private val eventBus: EventBus,
    private val logger: Logger,
) : BaseViewModel(env, view) {

    /**
     * Holds the starred action ids loaded from persistent storage.
     */
    private val starredIds = MutableStateFlow<Set<String>>(emptySet())

    /**
     * Monotonic sequence of runAction invocations. Guards late continuations
     * of a superseded run: unlike _runningActionId, it also distinguishes a
     * restart of the *same* action (A canceled -> A started again), where the
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
     * Raw actions loaded from the repository.
     */
    private val rawActions = MutableStateFlow<List<ActionModel>>(emptyList())

    /**
     * Holds the current list of actions displayed in the UI.
     * Merges raw actions with starred ids to update the isStarred flag.
     */
    val actions: StateFlow<List<ActionModel>> = rawActions.combine(starredIds) { actions, starredIds ->
        actions.map { it.copy(isStarred = it.id in starredIds) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
     * Mutable state flow controlling whether action descriptions are shown.
     */
    private val _showDescriptions = MutableStateFlow(true)
    val showDescriptions: StateFlow<Boolean> = _showDescriptions.asStateFlow()

    /**
     * Holds the action currently pending deletion confirmation.
     */
    private val _actionToDelete = MutableStateFlow<ActionModel?>(null)
    val actionToDelete = _actionToDelete.asStateFlow()

    /**
     * Tracks error loading state for data.
     */
    private val _errorLoad = MutableStateFlow(false)
    val errorLoad: StateFlow<Boolean> = _errorLoad.asStateFlow()

    /**
     * Version-sync banner state derived from comparing the installed CLI
     * version against `Constants.SUPPORTED_CLI_VERSION`.
     */
    private val _versionBanner = MutableStateFlow<VersionBannerState>(VersionBannerState.None)
    val versionBanner: StateFlow<VersionBannerState> = _versionBanner.asStateFlow()

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
        loadStarredIds()
        loadShowDescriptions()
        loadVersionBanner()
    }

    /**
     * Reloads the action list. Cancels any in-progress load and
     * starts a new one with a small artificial delay.
     */
    fun refresh(showLoader: Boolean = true) {
        loadJob?.cancel()
        loadData(showLoader)
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

                    AppEvent.DescriptionsVisibilityChanged -> {
                        logger.d { "Descriptions visibility changed, reloading preference." }
                        loadShowDescriptions()
                    }
                }
            }
        }
    }

    /**
     * Loads starred action ids from persistent storage into the StateFlow.
     */
    private fun loadStarredIds() {
        viewModelScope.launch {
            val str = env.bridge.sys.loadPreference?.invoke(PreferenceKey.StarredActions.name)
            starredIds.value = str?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        }
    }

    /**
     * Loads the "show descriptions" preference from persistent storage
     * and updates the corresponding state flow.
     */
    private fun loadShowDescriptions() {
        viewModelScope.launch {
            val str = env.bridge.sys.loadPreference?.invoke(PreferenceKey.ShowDescriptions.name)
            _showDescriptions.value = str?.toBoolean() ?: true
        }
    }

    /**
     * Fetches CLI status and sets the version-sync banner state by comparing
     * the installed CLI `major.minor` against `Constants.SUPPORTED_CLI_VERSION`.
     * Patch is ignored. Leaves [VersionBannerState.None] when the CLI is
     * missing or its version can't be parsed.
     */
    private fun loadVersionBanner() {
        viewModelScope.launch {
            val status = toolingRepository.getStatus() ?: return@launch
            _versionBanner.value = VersionBannerState.compare(
                status.version,
                Constants.SUPPORTED_CLI_VERSION,
            )
        }
    }

    /**
     * Loads the action list from the repository.
     * Serialized by [loadMutex] to prevent concurrent loads.
     */
    private fun loadData(showLoader: Boolean) {
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                _errorLoad.value = false
                if (showLoader) {
                    _isLoading.value = true
                    delay(1000.milliseconds)
                }
                try {
                    rawActions.value = actionRepository.loadActions()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.e(e) { "Load error" }
                    _errorLoad.value = true
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
     * Toggles the star state for an action and saves the new set to storage.
     */
    fun onToggleStar(id: String) {
        starredIds.update { current ->
            val newSet = if (current.contains(id)) current - id else current + id
            env.bridge.sys.savePreference?.invoke(PreferenceKey.StarredActions.name, newSet.joinToString(","))
            newSet
        }
    }

    /**
     * Runs the action with the specified id, updates the text, and pastes it back.
     * Cancels any previously running action before starting.
     *
     * Cancellation kills the previous CLI process via the CliProcess handle
     * (see CommandProvider Execute) — no 'stop' command is needed; the new
     * process's Rust RunGuard backstops the shutdown.
     */
    fun runAction(id: String) {
        val action = rawActions.value.find { it.id == id } ?: return

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
                    onCancel = { if (isActive) _notification.value = NotificationModel.actionCancelled(action.name) },
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
                    if (env.bridge.sys.language == "en" && e.message != null) {
                        _notification.value = NotificationModel.custom(action.name, e.message!!)
                    } else {
                        _notification.value = NotificationModel.actionFailed(action.name)
                    }
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
     * The coroutine unwinds -> invokeOnCancellation kills the CLI process
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
     * Opens the action's YAML configuration file in the platform's native editor.
     * Does nothing if the action has no file path or if the platform does not support opening files.
     */
    fun editAction(id: String) {
        val action = rawActions.value.find { it.id == id } ?: return
        viewModelScope.launch {
            val path = action.yamlPath ?: return@launch
            env.bridge.sys.openFile?.invoke(path)
        }
    }

    /**
     * Requests deletion by triggering the UI dialog.
     */
    fun requestDeleteAction(id: String) {
        _actionToDelete.value = rawActions.value.find { it.id == id }
    }

    /**
     * Cancels the deletion dialog.
     */
    fun cancelDeleteAction() {
        _actionToDelete.value = null
    }

    /**
     * Confirms and executes the deletion.
     */
    fun confirmDeleteAction() {
        val action = _actionToDelete.value ?: return
        _actionToDelete.value = null
        _expandedActionId.value = null

        val path = action.yamlPath ?: return

        viewModelScope.launch {
            val deleted = env.bridge.sys.deleteFile?.invoke(path) ?: false
            _notification.value = if (deleted) {
                rawActions.update { actions -> actions.filterNot { it.id == action.id } }
                NotificationModel.actionDeleteSuccess(action.name)
            } else {
                NotificationModel.actionDeleteFailed(action.name)
            }
        }
    }

    /**
     * Creates a new action file from the template and opens it in the editor.
     */
    fun createAction() {
        viewModelScope.launch {
            val status = toolingRepository.getStatus()
            val basePath = status?.actionsPath
            val pipelineVersion = status?.pipelineVersion?.removePrefix("v") ?: "0.0.2"
            val sys = env.bridge.sys

            if (basePath == null) {
                logger.e { "Cannot get actions path to create new action" }
                return@launch
            }

            val separator = if (basePath.endsWith("/") || basePath.endsWith("\\")) "" else "/"
            var counter = 0
            var fileName = "my-action"
            var fullPath = "$basePath$separator$fileName.yaml"

            while (sys.fileExists?.invoke(fullPath) == true) {
                counter++
                fileName = "my-action-$counter"
                fullPath = "$basePath$separator$fileName.yaml"
            }

            sys.writeFile?.invoke(
                fullPath,
                Constants.ACTION_TEMPLATE
                    .replace("{version}", pipelineVersion)
                    .replace("{name}", fileName),
            )
            sys.openFile?.invoke(fullPath)
        }
        refresh(showLoader = false)
    }
}
