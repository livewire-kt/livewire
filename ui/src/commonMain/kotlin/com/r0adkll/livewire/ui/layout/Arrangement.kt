package com.r0adkll.livewire.ui.layout

import androidx.compose.ui.unit.Dp
import com.r0adkll.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.Arrangement as ComposeArrangement

@Serializable
sealed interface Arrangement {

  @Serializable
  sealed interface Horizontal : Arrangement

  @Serializable
  sealed interface Vertical : Arrangement

  @Serializable
  sealed interface HorizontalOrVertical : Horizontal, Vertical

  @Serializable
  data object Start : Horizontal

  @Serializable
  data object End : Horizontal

  @Serializable
  data object Top : Vertical

  @Serializable
  data object Bottom : Vertical

  @Serializable
  data object Center : HorizontalOrVertical

  @Serializable
  data object SpaceBetween : HorizontalOrVertical

  @Serializable
  data object SpaceAround : HorizontalOrVertical

  @Serializable
  data object SpaceEvenly : HorizontalOrVertical

  @Serializable
  data class SpacedBy(
    @Serializable(with = DpSerializer::class) val space: Dp,
  ) : HorizontalOrVertical
}

fun Arrangement.Horizontal.toComposeUi(): ComposeArrangement.Horizontal = when (this) {
  Arrangement.Start -> ComposeArrangement.Start
  Arrangement.End -> ComposeArrangement.End
  Arrangement.Center -> ComposeArrangement.Center
  Arrangement.SpaceBetween -> ComposeArrangement.SpaceBetween
  Arrangement.SpaceAround -> ComposeArrangement.SpaceAround
  Arrangement.SpaceEvenly -> ComposeArrangement.SpaceEvenly
  is Arrangement.SpacedBy -> ComposeArrangement.spacedBy(space)
}

fun Arrangement.Vertical.toComposeUi(): ComposeArrangement.Vertical = when (this) {
  Arrangement.Top -> ComposeArrangement.Top
  Arrangement.Bottom -> ComposeArrangement.Bottom
  Arrangement.Center -> ComposeArrangement.Center
  Arrangement.SpaceBetween -> ComposeArrangement.SpaceBetween
  Arrangement.SpaceAround -> ComposeArrangement.SpaceAround
  Arrangement.SpaceEvenly -> ComposeArrangement.SpaceEvenly
  is Arrangement.SpacedBy -> ComposeArrangement.spacedBy(space)
}
