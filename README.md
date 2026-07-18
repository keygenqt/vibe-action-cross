# Vibe Action Cross

IDE integration for [Vibe Action](https://vibe-action.keygenqt.com/) — a single Compose Multiplatform UI,
shipped natively as both an IntelliJ Platform plugin and a VS Code extension.

Vibe Action itself works standalone via clipboard + OS tasks/external tools — no plugin required.
This project exists to remove that friction: instead of memorizing keybindings or hand-editing `tasks.json`/`External Tools.xml`,
you get a native panel inside the IDE that discovers available flows (built-in and custom YAML) and runs them with visible feedback.

## Why

- **Discovery** — see all available actions, including custom YAML flows, without configuring anything per-project
- **Feedback** — know what's happening while a flow runs, instead of waiting on a system notification
- **Native everywhere** — Jewel-themed inside IntelliJ, VS Code-themed inside its webview; same UI code, no visual mismatch with either host

## Requirements

- [`vibe-action`](https://crates.io/crates/vibe-action) CLI installed and available on `PATH`

## Architecture

One Kotlin Multiplatform / Compose Multiplatform codebase targets two hosts:

- **IntelliJ Platform Plugin** — Compose rendered via `ComposePanel` inside a Swing tool window, Jewel-themed
- **VS Code Extension** — same UI compiled to JS/Wasm, rendered inside the extension's webview

Platform-specific behavior (process execution, native dialogs, resources) goes through a `Bridge`, not `if`/`else`
branching in shared code. See [`cmp-ide-cross`](https://gitcode.com/keygenqt_vz/cmp-ide-cross)
for the architectural writeup this project builds on.

## Running the Apps

### IntelliJ Platform Plugin

```bash
./gradlew :plugin-idea:runIde
```

### VS Code Extension

```bash
./gradlew :plugin-ui:webApp:launchVscode
```
