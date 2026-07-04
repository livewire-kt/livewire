package com.livewire.ui.actions

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
data class ValueChangeAction(
  val identifier: String,
  val value: String = "",
) : LivewireAction

@Composable
fun valueChangeAction(
  onValueChange: (String) -> Unit,
): ValueChangeAction {
  val compositionKey = "onValueChange_${currentCompositeKeyHashCode}"
  val updatedOnValueChange by rememberUpdatedState(onValueChange)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<ValueChangeAction>()
      .filter { it.identifier == compositionKey }
      .collect {
        updatedOnValueChange(it.value)
      }
  }

  return remember(compositionKey) {
    ValueChangeAction(compositionKey)
  }
}
