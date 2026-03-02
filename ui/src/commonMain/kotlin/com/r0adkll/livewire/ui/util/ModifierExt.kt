package com.r0adkll.livewire.ui.util

import androidx.compose.ui.Modifier

inline fun Modifier.thenIf(predicate: Boolean, block: Modifier.() -> Modifier): Modifier {
  return if (predicate) {
    block(this)
  } else this
}

inline fun Modifier.thenIfElse(
  predicate: Boolean,
  ifTrue: Modifier.() -> Modifier,
  ifFalse: Modifier.() -> Modifier,
): Modifier {
  return if (predicate) {
    ifTrue(this)
  } else {
    ifFalse(this)
  }
}
