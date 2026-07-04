package com.livewire.ui.modifier

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.graphics.RectangleShape
import com.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class BorderModifier(
  val width: Float,
  @Serializable(with = ColorSerializer::class) val color: Color,
  val shape: Shape = RectangleShape,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.border(
      width = width.dp,
      color = color,
      shape = shape.toComposeUi(),
    )
  }
}

fun LivewireModifier.border(width: Dp, color: Color): LivewireModifier =
  then(BorderModifier(width.value, color))

fun LivewireModifier.border(width: Dp, color: Color, shape: Shape): LivewireModifier =
  then(BorderModifier(width.value, color, shape))
