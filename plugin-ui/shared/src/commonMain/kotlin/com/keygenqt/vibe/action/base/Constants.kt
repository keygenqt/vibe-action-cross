/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.base

/**
 * Shared build/version metadata, referenced across views (e.g. About screen).
 */
object Constants {
    const val VERSION = "0.0.9"
    const val AUTHOR = "Vitaliy Zarubin (keygenqt)"

    /**
     * Latest vibe-action CLI version this plugin supports.
     * The version-sync check compares `major.minor` against the installed
     * CLI's `major.minor` (patch is ignored). Bump per CLI release.
     */
    const val SUPPORTED_CLI_VERSION = "0.3.1"

    /**
     * Docs opened by the "Update plugin" banner button.
     */
    const val URL_DOCS_PLUGIN = "https://vibe-action.keygenqt.com/docs/ide-plugin.html"

    /**
     * Docs opened by the "Update CLI" banner button.
     */
    const val URL_DOCS_CLI = "https://vibe-action.keygenqt.com/docs/getting-started.html"

    /**
     * Template for custom action configuration.
     */

    /**
     * Template for custom action configuration.
     */
    val ACTION_TEMPLATE = """
        # Link: https://vibe-action.keygenqt.com/docs/pipeline-yaml.md

        version: {version}

        name: {name}
        about: Short description

        api:
          output: dialog # replace, clipboard, dialog
          input: query_prompt # query_raw, query_prompt, query_file_path, query_project_path, query_line, query_image

        actions:
          - tag: tag_result
            run: small # cmd | value | tiny | small | medium | large | vision
            val:
              - name: input
                data: query_prompt
            action: |
              [Task]
              Greet the user and suggest a name for the project.
              Answer briefly, in one sentence.

              [Input]
              {input}
    """.trimIndent()
}
