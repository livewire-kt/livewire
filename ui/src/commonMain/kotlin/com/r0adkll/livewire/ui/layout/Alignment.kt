package com.r0adkll.livewire.ui.layout

import kotlinx.serialization.Serializable

@Serializable
sealed interface Alignment {
  @Serializable sealed interface Horizontal : Alignment
  @Serializable sealed interface Vertical : Alignment

  @Serializable data object TopStart: Alignment
  @Serializable data object TopCenter: Alignment
  @Serializable data object TopEnd: Alignment
  @Serializable data object CenterStart: Alignment
  @Serializable data object Center: Alignment
  @Serializable data object CenterEnd: Alignment
  @Serializable data object BottomStart: Alignment
  @Serializable data object BottomCenter: Alignment
  @Serializable data object BottomEnd: Alignment

  @Serializable data object Top: Vertical
  @Serializable data object CenterVertically: Vertical
  @Serializable data object Bottom: Vertical

  @Serializable data object Start: Horizontal
  @Serializable data object CenterHorizontally: Horizontal
  @Serializable data object End: Horizontal
}
