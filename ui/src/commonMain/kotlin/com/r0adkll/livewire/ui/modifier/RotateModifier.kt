package com.r0adkll.livewire.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.r0adkll.livewire.annotations.LivewireSerializer
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class RotateModifier(
  val degrees: Float,
) : LivewireModifier.Element {
  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.rotate(degrees)
  }

}

fun LivewireModifier.rotate(degrees: Float): LivewireModifier {
  return then(RotateModifier(degrees))
}
