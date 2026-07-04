package com.livewire.ui.composition

import androidx.compose.runtime.Recomposer
import co.touchlab.stately.collections.ConcurrentMutableMap

object LivewireRecomposers {
  private val recomposers = ConcurrentMutableMap<Recomposer, Unit>()

  fun register(recomposer: Recomposer) {
    recomposers[recomposer] = Unit
  }

  fun unregister(recomposer: Recomposer) {
    recomposers.remove(recomposer)
  }

  operator fun contains(recomposer: Any?): Boolean =
    recomposer is Recomposer && recomposers.containsKey(recomposer)
}
