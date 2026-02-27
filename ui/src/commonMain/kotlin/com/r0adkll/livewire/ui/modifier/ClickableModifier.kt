package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.actions.clickAction
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class ClickableModifier(
  val action: ClickAction,
  val enabled: Boolean = true,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    val scope = rememberCoroutineScope()
    val eventDispatcher = LocalLivewireActionDispatcher.current
    return then.clickable(enabled = enabled) {
      scope.launch { eventDispatcher.dispatch(action) }
    }
  }
}

fun LivewireModifier.clickable(
  action: ClickAction,
  enabled: Boolean = true,
): LivewireModifier =
  then(ClickableModifier(action, enabled))
