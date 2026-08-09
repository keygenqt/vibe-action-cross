const vscode = require('vscode')
const path = require('path')
const fs = require('fs')
const { spawn } = require('child_process')

/**
 * Path to vibe-action CLI binary. Uses VIBE_ACTION_CLI_PATH env var for debug builds,
 * falls back to system PATH lookup.
 */
const cliPath = process.env.VIBE_ACTION_CLI_PATH || 'vibe-action'

/**
 * Live CLI child processes keyed by invocation id (passed from Kotlin).
 * Enables killCli to target a specific process; entries are removed on exit.
 */
const cliProcesses = new Map()

/**
 * Handles runCli command: spawns vibe-action process and streams NDJSON output.
 * All stdout lines are forwarded as-is — the Kotlin side decides what is valid JSON.
 * procId registers the process for killCli; concurrent invocations are independent.
 */
function handleRunCli(webview, args, requestId) {
  const [cmdArgs, eventTarget, doneTarget, procId] = args

  const proc = spawn(cliPath, cmdArgs || [], {
    env: { ...process.env, VIBE_LOG_TYPE: 'json' }
  })

  if (procId != null) {
    cliProcesses.set(procId, proc)
  }

  // Guard against double onDone: Node fires both 'error' and 'close'
  // when spawn fails (e.g. ENOENT — CLI not found in PATH).
  let finished = false
  const done = (code, stdout = '', stderr = '') => {
    if (finished) return
    finished = true
    if (procId != null) cliProcesses.delete(procId)
    webview.postMessage({ target: doneTarget, args: [code, { first: stdout, second: stderr }] })
  }

  let buffer = ''
  let stdoutAccumulator = ''
  let stderrAccumulator = ''

  proc.stdout.on('data', data => {
    const str = data.toString()
    buffer += str
    stdoutAccumulator += str
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() || ''
    lines.filter(l => l.trim()).forEach(line => {
      webview.postMessage({ target: eventTarget, args: [line] })
    })
  })

  proc.stderr.on('data', data => {
    const str = data.toString()
    stderrAccumulator += str
    console.error('stderr:', str)
  })

  proc.on('error', err => {
    console.error('spawn error:', err)
    done(-1, '', err.message)
  })

  proc.on('close', code => {
    // Flush the trailing line if the process didn't terminate it with \n
    const tail = buffer.trim()
    if (tail) {
      webview.postMessage({ target: eventTarget, args: [tail] })
    }
    // 'close' may fire with null code when the process was killed by a signal
    done(code == null ? -1 : code, stdoutAccumulator, stderrAccumulator)
  })

  if (requestId != null) {
    webview.postMessage({ requestId, result: true })
  }
}

/**
 * Handles killCli: terminates a previously spawned CLI process by id.
 * No-op if the process already exited — the webview has already dropped its
 * callback slot by then, so the resulting late cliDone is ignored there.
 */
function handleKillCli(args) {
  const [procId] = args || []
  const proc = cliProcesses.get(procId)
  if (proc) {
    cliProcesses.delete(procId)
    proc.kill()
  }
}

/**
 * Opens a file in the VS Code editor via vscode.window.showTextDocument.
 * preview: false ensures it opens a new tab instead of replacing an unpinned one.
 */
async function handleOpenFile(webview, args, requestId) {
  const [filePath] = args
  const uri = vscode.Uri.file(filePath)

  try {
    await vscode.window.showTextDocument(uri, { preview: false })
    if (requestId != null) webview.postMessage({ requestId, result: true })
  } catch (err) {
    console.error('openFile failed:', err)
    if (requestId != null) webview.postMessage({ requestId, result: false })
  }
}

/**
 * Registers the sidebar Webview provider and bridges postMessage to VS Code API.
 */
function activate(context) {
  const bundleDir = path.join(context.extensionPath, 'productionExecutable')

  const provider = {
    resolveWebviewView(webviewView) {
      webviewView.webview.options = {
        enableScripts: true,
        localResourceRoots: [vscode.Uri.file(bundleDir)],
      }

      const file = path.join(bundleDir, 'index.html')
      const baseUri = webviewView.webview.asWebviewUri(vscode.Uri.file(bundleDir)) + '/'
      webviewView.webview.html = fs
          .readFileSync(file, 'utf8')
          .replace('<head>', `<head><base href="${baseUri}"><script>window.__vscodeLang = "${(vscode.env.language)}";</script>`)
          .replace(/(href|src)="([^"]+)"/g, (match, attr, rel) => {
            if (/^(https?:|data:|#)/.test(rel)) return match
            const uri = webviewView.webview.asWebviewUri(vscode.Uri.file(path.join(bundleDir, rel)))
            return `${attr}="${uri}"`
          })

      const themeListener = vscode.window.onDidChangeActiveColorTheme(() => {
        webviewView.webview.postMessage({ target: 'themeChanged' })
      })
      context.subscriptions.push(themeListener)
      webviewView.onDidDispose(() => {
        themeListener.dispose()
        for (const proc of cliProcesses.values()) proc.kill()
        cliProcesses.clear()
      })

      const handlers = {
        loadPreference: (key) => {
          // VS Code returns undefined if the key doesn't exist.
          // We return null to map correctly to Kotlin's String?
          const val = context.globalState.get(key)
          return val === undefined ? null : val
        },
        savePreference: (key, value) => {
          return context.globalState.update(key, value)
        },
        writeFile: (p, content) => {
          try {
            fs.writeFileSync(p, content, 'utf8')
            return true
          } catch (e) {
            return false
          }
        },
        fileExists: (p) => fs.existsSync(p),
        deleteFile: (p) => {
          try {
            fs.unlinkSync(p)
            return true
          } catch (e) {
            return false
          }
        },
        getClipboardText: () => vscode.env.clipboard.readText(),
        setClipboardText: (text) => vscode.env.clipboard.writeText(text),
        getSelectedText: () => {
          const editor = vscode.window.activeTextEditor
          return editor ? editor.document.getText(editor.selection) : ""
        },
        getDialogText: async () => {
          const result = await vscode.window.showInputBox({})
          return result === undefined ? null : result
        },
        showTextDialog: async (title, text) => {
          const safeTitle = title.toLowerCase().replace(/\s+/g, '-')
          const uri = vscode.Uri.parse(`untitled:${safeTitle}.txt`)
          const doc = await vscode.workspace.openTextDocument(uri)
          const editor = await vscode.window.showTextDocument(doc)
          await editor.edit(editBuilder => {
            const firstLine = doc.lineAt(0)
            const lastLine = doc.lineAt(doc.lineCount - 1)
            const range = new vscode.Range(firstLine.range.start, lastLine.range.end)
            editBuilder.replace(range, text)
          })
        },
        replaceSelectedText: async (newText) => {
          const editor = vscode.window.activeTextEditor
          if (editor && typeof newText === 'string') {
            await editor.edit(editBuilder => {
              editBuilder.replace(editor.selection, newText)
            })
          }
        }
      }

      webviewView.webview.onDidReceiveMessage(async message => {
        const { target, args, requestId } = message

        if (target === 'runCli') return handleRunCli(webviewView.webview, args, requestId)
        if (target === 'killCli') return handleKillCli(args)
        if (target === 'openFile') return handleOpenFile(webviewView.webview, args, requestId)

        const handler = handlers[target]
        if (handler) {
          const result = await handler(...args)
          if (requestId != null) {
            webviewView.webview.postMessage({ requestId, result })
          }
          return
        }

        if (typeof vscode.window[target] === 'function') {
          const result = await vscode.window[target](...args)
          if (requestId != null) {
            webviewView.webview.postMessage({ requestId, result })
          }
        }
      })
    },
  }

  context.subscriptions.push(
      vscode.window.registerWebviewViewProvider('vibe-action.sidebarView', provider, {
        webviewOptions: { retainContextWhenHidden: true },
      })
  )
}

/**
 * Cleanup on extension deactivation.
 */
function deactivate() {}

module.exports = { activate, deactivate }
