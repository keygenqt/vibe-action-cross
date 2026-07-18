package com.keygenqt.vibe.action.base

import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.bridge.ViewBridge
import com.keygenqt.vibe.action.bridge.bridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base ViewModel with a coroutine scope that survives configuration changes.
 */
open class BaseViewModel(
    val env: Environment,
    val view: PlatformView
) {
    /**
     * ViewBridge for this ViewModel's platform view.
     */
    protected val bridge: ViewBridge = view.bridge(env)

    /**
     * Coroutine scope tied to this ViewModel's lifecycle.
     */
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Cancels the viewModelScope. Call when the ViewModel is no longer needed.
     */
    open fun dispose() {
        viewModelScope.cancel()
    }
}
