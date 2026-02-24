package com.r0adkll.livewire.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.Serializable

@Immutable
@Serializable
class ClickAction(
  val identifier: String,
) : LivewireAction {
}

@Composable
fun clickAction(
  onClick: () -> Unit,
): ClickAction {
  val compositionKey = "click_${currentCompositeKeyHashCode}"
  val updatedOnClick by rememberUpdatedState(onClick)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<ClickAction>()
      .filter { it.identifier == compositionKey }
      .collect {
        updatedOnClick()
      }
  }

  return remember(compositionKey) {
    ClickAction(compositionKey)
  }
}
