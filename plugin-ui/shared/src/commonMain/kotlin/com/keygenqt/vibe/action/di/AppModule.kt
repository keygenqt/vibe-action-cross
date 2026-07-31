/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.di

import com.keygenqt.vibe.action.base.EventBus
import com.keygenqt.vibe.action.bridge.PlatformView
import com.keygenqt.vibe.action.command.ActionRepository
import com.keygenqt.vibe.action.command.CommandProvider
import com.keygenqt.vibe.action.command.ToolingRepository
import com.keygenqt.vibe.action.view.about.AboutViewModel
import com.keygenqt.vibe.action.view.main.MainViewModel
import com.keygenqt.vibe.action.view.settings.SettingsViewModel
import org.koin.dsl.module

/**
 * Top-level Koin module that aggregates logging and ViewModels.
 */
val appModule = module {
    // Kermit logging
    includes(loggingModule)

    // Shared event bus for cross-ViewModel communication.
    single { EventBus() }

    // Registers CommandProvider as singleton with injected dependencies.
    single { CommandProvider(get(), get()) }

    // Repositories
    single { ToolingRepository(get(), get()) }
    single { ActionRepository(get(), get(), get(), get()) }

    // ViewModels
    single {
        MainViewModel(
            env = get(),
            view = PlatformView.Main,
            actionRepository = get(),
            toolingRepository = get(),
            eventBus = get(),
            logger = get(),
        )
    }

    single {
        SettingsViewModel(
            env = get(),
            view = PlatformView.Settings,
            toolingRepository = get(),
            eventBus = get(),
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
}
