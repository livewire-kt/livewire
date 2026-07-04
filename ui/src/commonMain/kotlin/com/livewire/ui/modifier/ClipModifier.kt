package com.livewire.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.graphics.Shape
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class ClipModifier(
  val shape: Shape,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.clip(shape.toComposeUi())
  }
}

fun LivewireModifier.clip(shape: Shape): LivewireModifier =
  then(ClipModifier(shape))
