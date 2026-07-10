package com.livewire.ui.composition

import kotlin.coroutines.CoroutineContext

object LivewireComposition : CoroutineContext.Element, CoroutineContext.Key<LivewireComposition> {
  override val key: CoroutineContext.Key<LivewireComposition> get() = this
}
