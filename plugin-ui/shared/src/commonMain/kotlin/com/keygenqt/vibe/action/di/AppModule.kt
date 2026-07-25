/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.di

import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.CommandProvider
import com.keygenqt.vibe.action.view.about.AboutViewModel
import com.keygenqt.vibe.action.view.history.HistoryViewModel
import com.keygenqt.vibe.action.view.historyDetail.HistoryDetailViewModel
import com.keygenqt.vibe.action.view.main.MainViewModel
import com.keygenqt.vibe.action.view.settings.SettingsViewModel
import org.koin.dsl.module

/**
 * Top-level Koin module that aggregates logging and ViewModels.
 */
val appModule = module {
    // Kermit logging
    includes(loggingModule)

    // Registers CommandProvider as singleton with injected dependencies.
    single { CommandProvider(get(), get()) }

    // ViewModels
    single {
        MainViewModel(
            env = get(),
            view = PlatformView.Main,
            commandProvider = get(),
            logger = get(),
        )
    }
    single {
        SettingsViewModel(
            env = get(),
            view = PlatformView.Settings,
            logger = get(),
        )
    }
    single {
        HistoryViewModel(
            env = get(),
            view = PlatformView.History,
            logger = get(),
        )
    }
    single {
        AboutViewModel(
            env = get(),
            view = PlatformView.About,
            logger = get(),
        )
    }
    factory { (runId: String) ->
        HistoryDetailViewModel(
            env = get(),
            view = PlatformView.HistoryDetail,
            logger = get(),
            runId = runId,
        )
    }
}
