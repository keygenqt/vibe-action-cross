# Changelog

All notable changes to Vibe Action will be documented in this file.

## [0.1.0] - 2026-10-04

### ⚡ Refactoring

- Carry group about through models, restore plain scroll scaffold

### 📚 Documentation

- Rewrite About texts around Vibe Action, add section about strings

### 🚀 Features

- Collapsible action sections with persistence, icons and descriptions

## [0.0.10] - 2026-10-02

### 🚀 Features

- Up support 0.4.0 vibe-action

## [0.0.9] - 2026-09-29

### ⚡ Refactoring

- Resolve action queries via actionapiinput enum and add source mapping

### 🚀 Features

- Support action groups from CLI

## [0.0.7] - 2026-08-22

### 🐛 Fixes

- Remove old build
- Correct plugin archive filename and packaging task dependencies
- Scope koin per window and dispose vs code bridge listeners

### 📚 Documentation

- Add changelog documenting fixes and features for releases

### 🚀 Features

- Init project
- Add spotless, license headers, and plugin bridges
- Add live theme-aware color scheme for IDEA and VS Code
- Add about, history, settings screens and update icons
- Add action management ui and translations
- Add settings, history, and cli execution features
- Add cli command execution and file existence checks
- Add common refresh and selected text handling
- Add clipboard, api metadata, and executeaction support
- Add notifications, clipboard, dialog, and action handling
- Upgrade settings screen
- Support concurrent CLI processes with cancellable per-invocation handles instead of a global busy flag
- Add action starring, editing, deletion, and appearance settings
- Add preview
- Add preview.svg
- Add error view, add depends compose
- Add language support and full cli output capture
- Add dialog text input action source
- Update plugin for vibe-action 0.2.0
- Update plugin to 0.0.5 and revise action template schema
- Include flow version and yaml path in action template and metadata
- Add async confirm dialog and stdin writer for cli prompts
- Add version banner and fetch plugin version from system bridges
- Add version banner for cli/plugin mismatches and openurl bridge for external links

