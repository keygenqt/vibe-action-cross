/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.base

/**
 * Shared build/version metadata, referenced across views (e.g. About screen).
 */
object Constants {
    const val VERSION = "0.0.6"
    const val AUTHOR = "Vitaliy Zarubin (keygenqt)"

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
