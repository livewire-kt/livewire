package com.livewire.ui.composition

private fun performanceNow(): Double = js("performance.now()")

internal actual fun nanoTime(): Long = (performanceNow() * 1_000_000).toLong()
