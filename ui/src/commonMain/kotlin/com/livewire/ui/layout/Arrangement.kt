package com.livewire.ui.layout

import androidx.compose.ui.unit.Dp
import com.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.Arrangement as ComposeArrangement
import androidx.compose.runtime.Immutable

@Immutable
@Serializable
sealed interface Arrangement {

  @Immutable
  @Serializable
  sealed interface Horizontal : Arrangement

  @Immutable
  @Serializable
  sealed interface Vertical : Arrangement

  @Immutable
  @Serializable
  sealed interface HorizontalOrVertical : Horizontal, Vertical

  @Immutable
  @Serializable
  data object Start : Horizontal

  @Immutable
  @Serializable
  data object End : Horizontal

  @Immutable
  @Serializable
  data object Top : Vertical

  @Immutable
  @Serializable
  data object Bottom : Vertical

  @Immutable
  @Serializable
  data object Center : HorizontalOrVertical

  @Immutable
  @Serializable
  data object SpaceBetween : HorizontalOrVertical

  @Immutable
  @Serializable
  data object SpaceAround : HorizontalOrVertical

  @Immutable
  @Serializable
  data object SpaceEvenly : HorizontalOrVertical

  @Immutable
  @Serializable
  @Deprecated("Use spacedBy instead.", replaceWith = ReplaceWith("spacedBy(space)", "com.livewire.ui.layout.Arrangement.spacedBy"))
  data class SpacedBy(
    @Serializable(with = DpSerializer::class) val space: Dp,
  ) : HorizontalOrVertical

  companion object {
    @Suppress("DEPRECATION")
    fun spacedBy(space: Dp): HorizontalOrVertical = SpacedBy(space)
  }
}

@Suppress("DEPRECATION")
fun Arrangement.Horizontal.toComposeUi(): ComposeArrangement.Horizontal = when (this) {
  Arrangement.Start -> ComposeArrangement.Start
  Arrangement.End -> ComposeArrangement.End
  Arrangement.Center -> ComposeArrangement.Center
  Arrangement.SpaceBetween -> ComposeArrangement.SpaceBetween
  Arrangement.SpaceAround -> ComposeArrangement.SpaceAround
  Arrangement.SpaceEvenly -> ComposeArrangement.SpaceEvenly
  is Arrangement.SpacedBy -> ComposeArrangement.spacedBy(space)
}

@Suppress("DEPRECATION")
fun Arrangement.Vertical.toComposeUi(): ComposeArrangement.Vertical = when (this) {
  Arrangement.Top -> ComposeArrangement.Top
  Arrangement.Bottom -> ComposeArrangement.Bottom
  Arrangement.Center -> ComposeArrangement.Center
  Arrangement.SpaceBetween -> ComposeArrangement.SpaceBetween
  Arrangement.SpaceAround -> ComposeArrangement.SpaceAround
  Arrangement.SpaceEvenly -> ComposeArrangement.SpaceEvenly
  is Arrangement.SpacedBy -> ComposeArrangement.spacedBy(space)
}
