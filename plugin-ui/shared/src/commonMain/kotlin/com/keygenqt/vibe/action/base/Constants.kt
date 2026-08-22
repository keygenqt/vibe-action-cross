/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.base

/**
 * Shared build/version metadata, referenced across views (e.g. About screen).
 */
object Constants {
    const val VERSION = "0.0.7"
    const val AUTHOR = "Vitaliy Zarubin (keygenqt)"

    /**
     * Latest vibe-action CLI version this plugin supports.
     * The version-sync check compares `major.minor` against the installed
     * CLI's `major.minor` (patch is ignored). Bump per CLI release.
     */
    const val SUPPORTED_CLI_VERSION = "0.2.3"

    /**
     * Docs opened by the "Update plugin" banner button.
     */
    const val URL_DOCS_PLUGIN = "https://vibe-action.keygenqt.com/docs/vibe-action-cross.html"

    /**
     * Docs opened by the "Update CLI" banner button.
     */
    const val URL_DOCS_CLI = "https://vibe-action.keygenqt.com/docs/getting-started.html"

    /**
     * Template for custom action configuration.
     */
    val ACTION_TEMPLATE = """
        # Link: https://vibe-action.keygenqt.com/docs/action-structure.html
        
        version: {version}
        
        name: {name}
        about: Short description
        
        api:
          output: dialog # replace, clipboard, dialog
          input: query|prompt # raw (select), prompt, file_path, project_path, line, image
          
        actions:
          - tag: tag_step1
            run: small # cmd | value | small | medium | large | vision | tiny
            expect: string # string | list
            action: Greet the user {query|prompt}
    """.trimIndent()
}
