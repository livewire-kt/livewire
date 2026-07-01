package com.r0adkll.livewire.plugin.recomposition

import kotlin.time.TimeSource

internal object MonotonicClock {
  private val mark = TimeSource.Monotonic.markNow()
  fun elapsedMillis(): Long = mark.elapsedNow().inWholeMilliseconds
}
