package com.r0adkll.livewire.ui.modifier

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.annotations.LivewireSerializer
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class AnimateContentSizeModifier : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return then.animateContentSize()
  }
}

fun LivewireModifier.animateContentSize(): LivewireModifier =
  then(AnimateContentSizeModifier())
