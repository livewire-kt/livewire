package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@Serializable
internal class BackgroundModifier(
  @Serializable(with = ColorSerializer::class) val color: Color,
  val shape: Shape? = null,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.background(
      color = color,
      shape = shape?.toComposeUi() ?: RectangleShape,
    )
  }
}

fun LivewireModifier.background(color: Color): LivewireModifier =
  then(BackgroundModifier(color))

fun LivewireModifier.background(color: Color, shape: Shape): LivewireModifier =
  then(BackgroundModifier(color, shape))
