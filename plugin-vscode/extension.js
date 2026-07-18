const vscode = require('vscode')
const path = require('path')
const fs = require('fs')

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

      // Universal bridge: routes postMessage commands to VS Code API dynamically
      webviewView.webview.onDidReceiveMessage(async message => {
        const { target, args, requestId } = message
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
