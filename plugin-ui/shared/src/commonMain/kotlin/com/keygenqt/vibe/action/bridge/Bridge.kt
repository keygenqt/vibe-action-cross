package com.keygenqt.vibe.action.bridge

/**
 * Aggregates the system bridge and view-specific bridges into a single container.
 */
data class Bridge(
    val res: ResBridge,
    val sys: SysBridge,
    val vws: HashMap<PlatformView, ViewBridge>,
)
