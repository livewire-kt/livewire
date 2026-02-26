package com.r0adkll.livewire.ui.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.r0adkll.livewire.annotations.LivewireSerializer
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class AlphaModifier(
  val alpha: Float,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.alpha(alpha)
  }
}

fun LivewireModifier.alpha(alpha: Float): LivewireModifier =
  then(AlphaModifier(alpha))
