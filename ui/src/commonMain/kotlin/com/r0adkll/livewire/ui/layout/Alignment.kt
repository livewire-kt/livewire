package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface Alignment {
  @Immutable
  @Serializable
  sealed interface Horizontal : Alignment

  @Immutable
  @Serializable
  sealed interface Vertical : Alignment

  @Immutable
  @Serializable
  data object TopStart : Alignment

  @Immutable
  @Serializable
  data object TopCenter : Alignment

  @Immutable
  @Serializable
  data object TopEnd : Alignment

  @Immutable
  @Serializable
  data object CenterStart : Alignment

  @Immutable
  @Serializable
  data object Center : Alignment

  @Immutable
  @Serializable
  data object CenterEnd : Alignment

  @Immutable
  @Serializable
  data object BottomStart : Alignment

  @Immutable
  @Serializable
  data object BottomCenter : Alignment

  @Immutable
  @Serializable
  data object BottomEnd : Alignment

  @Immutable
  @Serializable
  data object Top : Vertical

  @Immutable
  @Serializable
  data object CenterVertically : Vertical

  @Immutable
  @Serializable
  data object Bottom : Vertical

  @Immutable
  @Serializable
  data object Start : Horizontal

  @Immutable
  @Serializable
  data object CenterHorizontally : Horizontal

  @Immutable
  @Serializable
  data object End : Horizontal
}
