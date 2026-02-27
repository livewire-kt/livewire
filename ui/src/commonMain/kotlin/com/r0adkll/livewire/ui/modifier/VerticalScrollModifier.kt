package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.annotations.LivewireSerializer
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class VerticalScrollModifier(
  val enabled: Boolean = true,
  val reverseScrolling: Boolean = false,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    val scrollState = rememberScrollState()
    return then.verticalScroll(
      state = scrollState,
      enabled = enabled,
      reverseScrolling = reverseScrolling,
    )
  }
}

fun LivewireModifier.verticalScroll(
  enabled: Boolean = true,
  reverseScrolling: Boolean = false,
): LivewireModifier =
  then(VerticalScrollModifier(enabled, reverseScrolling))
