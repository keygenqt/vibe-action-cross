/**
 * SPDX-FileCopyrightText: 2026 Vitaliy Zarubin <keygenqt@yandex.ru>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.keygenqt.vibe.action.base

/**
 * Shared build/version metadata, referenced across views (e.g. About screen).
 */
object Constants {
    const val VERSION = "0.0.1"
    const val AUTHOR = "Vitaliy Zarubin (keygenqt)"

    val ACTION_TEMPLATE = """
        name: {name}
        about: Short description
        # notify: true

        # args:
        #   - name: input
        #     short: i
        #     input: string
        #     help: Input text
        #     default: 'default'

        api:
          output: dialog  # replace | clipboard | dialog
          # args:
          #   input: selection

        actions:
          - tag: tag_step1
            run: value  # cmd | value | tiny | small | medium | large | vision
            expect: string  # string | list
            action: Hello, World!
    """.trimIndent()
}
