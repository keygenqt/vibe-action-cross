/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import com.keygenqt.vibe.action.bridge.Environment
import com.keygenqt.vibe.action.bridge.ViewEnvironment
import com.keygenqt.vibe.action.bridge.ViewScope
import com.keygenqt.vibe.action.di.appModule
import com.keygenqt.vibe.action.view.about.AboutView
import com.keygenqt.vibe.action.view.history.HistoryView
import com.keygenqt.vibe.action.view.historyDetail.HistoryDetailView
import com.keygenqt.vibe.action.view.main.MainView
import com.keygenqt.vibe.action.view.settings.SettingsView
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import org.koin.dsl.module

/**
 * Type-safe navigation routes — serializable destinations for Navigation 3.
 */
@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Main : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object History : AppRoute

    @Serializable
    data class HistoryDetail(val runId: String) : AppRoute

    @Serializable
    data object About : AppRoute
}

/**
 * Bootstraps the environment and provides it to the composable tree via ViewEnvironment and Koin.
 */
@Composable
fun InitApp(
    environment: Environment,
    composable: @Composable ViewScope.() -> Unit,
) {
    val lifecycleOwner = remember {
        object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.RESUMED
            }
        }
    }
    CompositionLocalProvider(
        LocalLifecycleOwner provides lifecycleOwner,
        ViewEnvironment provides environment,
    ) {
        KoinApplication(
            KoinConfiguration {
                logger(KermitKoinLogger(Logger.withTag("Koin")))
                modules(
                    appModule,
                    module {
                        single { environment }
                    },
                )
            },
        ) {
            ViewScope().composable()
        }
    }
}

/**
 * Root navigation graph — routes between Main and Settings views.
 *
 * Uses a plain in-memory back stack (no save/restore across full recomposition
 * teardown). None of our target platforms (IntellJPlugin, Desktop, VSExtension, Web)
 * benefit from Navigation3's serialization-based restore right now, and on the
 * IntelliJ plugin it actively conflicts with the IDE's own bundled compose-runtime
 * (androidx.savedstate loaded under two different PluginClassLoaders).
 */
@Composable
fun ViewScope.RootAppDispatcher() {
    val backStack = remember { NavBackStack<AppRoute>(AppRoute.Main) }

    NavDisplay(backStack = backStack) { route ->
        NavEntry(route) {
            when (route) {
                AppRoute.Main -> MainView(
                    onNavigateToSettings = { backStack.add(AppRoute.Settings) },
                    onNavigateToHistory = { backStack.add(AppRoute.History) },
                    onNavigateToAbout = { backStack.add(AppRoute.About) },
                )
                AppRoute.Settings -> SettingsView(
                    onBack = { backStack.removeLastOrNull() },
                )
                AppRoute.History -> HistoryView(
                    onBack = { backStack.removeLastOrNull() },
                    onOpenDetail = { runId -> backStack.add(AppRoute.HistoryDetail(runId)) },
                )
                is AppRoute.HistoryDetail -> HistoryDetailView(
                    runId = route.runId,
                    onBack = { backStack.removeLastOrNull() },
                )
                AppRoute.About -> AboutView(
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        }
    }
}
