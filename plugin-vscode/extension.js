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
 * Handles runCli command: spawns vibe-action process and streams NDJSON output.
 */
function handleRunCli(webview, args, requestId) {
  const [cmdArgs, eventTarget, doneTarget] = args

  const proc = spawn(cliPath, cmdArgs || [], {
    env: { ...process.env, VIBE_LOG_TYPE: 'json' }
  })

  let buffer = ''
  proc.stdout.on('data', data => {
    buffer += data.toString()
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() || ''
    lines.filter(l => l.trim()).forEach(line => {
      if (line.startsWith('{')) {
        console.log('JSON line:', line)  // Временный лог
        webview.postMessage({ target: eventTarget, args: [line] })
      }
    })
  })

  proc.stderr.on('data', data => {
    console.error('stderr:', data.toString())
  })

  proc.on('error', err => {
    console.error('spawn error:', err)
    webview.postMessage({ target: doneTarget, args: [-1] })
  })

  proc.on('close', code => {
    webview.postMessage({ target: doneTarget, args: [code] })
  })

  if (requestId != null) {
    webview.postMessage({ requestId, result: true })
  }
}

/**
 * Opens a file in the VS Code editor via vscode.window.showTextDocument.
 */
function handleOpenFile(webview, args, requestId) {
  const [filePath] = args
  const uri = vscode.Uri.file(filePath)
  vscode.window.showTextDocument(uri)
  if (requestId != null) {
    webview.postMessage({ requestId, result: true })
  }
}

/**
 * Registers the sidebar Webview provider and bridges postMessage to VS Code API.
 */
function activate(context) {
  const bundleDir = path.join(context.extensionPath, 'productionExecutable')

  const provider = {
    resolveWebviewView(webviewView) {
      // Allow scripts and restrict resource loading to the plugin-ui build output
      webviewView.webview.options = {
        enableScripts: true,
        localResourceRoots: [vscode.Uri.file(bundleDir)],
      }

      // Load index.html and remap asset paths to secure VS Code URIs
      const file = path.join(bundleDir, 'index.html')
      const baseUri = webviewView.webview.asWebviewUri(vscode.Uri.file(bundleDir)) + '/'
      webviewView.webview.html = fs
          .readFileSync(file, 'utf8')
          .replace('<head>', `<head><base href="${baseUri}">`)
          .replace(/(href|src)="([^"]+)"/g, (match, attr, rel) => {
            if (rel.startsWith('http')) return match
            const uri = webviewView.webview.asWebviewUri(vscode.Uri.file(path.join(bundleDir, rel)))
            return `${attr}="${uri}"`
          })

      // Push a themeChanged broadcast to the webview whenever the user switches VS Code's theme
      const themeListener = vscode.window.onDidChangeActiveColorTheme(() => {
        webviewView.webview.postMessage({ target: 'themeChanged' })
      })
      context.subscriptions.push(themeListener)
      webviewView.onDidDispose(() => themeListener.dispose())

      // Universal bridge: routes postMessage commands to VS Code API dynamically
      webviewView.webview.onDidReceiveMessage(async message => {
        const { target, args, requestId } = message

        if (target === 'runCli') {
          handleRunCli(webviewView.webview, args, requestId)
          return
        }

        if (target === 'openFile') {
          handleOpenFile(webviewView.webview, args, requestId)
          return
        }

        if (typeof vscode.window[target] !== 'function') return
        const result = await vscode.window[target](...args)
        if (requestId != null) {
          webviewView.webview.postMessage({ requestId, result })
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
