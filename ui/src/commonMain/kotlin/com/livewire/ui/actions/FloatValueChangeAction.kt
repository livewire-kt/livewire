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
data class FloatValueChangeAction(
  val identifier: String,
  val value: Float = 0f,
) : LivewireAction

@Composable
fun floatValueChangeAction(
  onValueChange: (Float) -> Unit,
): FloatValueChangeAction {
  val compositionKey = "onFloatValueChange_${currentCompositeKeyHashCode}"
  val updatedOnValueChange by rememberUpdatedState(onValueChange)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<FloatValueChangeAction>()
      .filter { it.identifier == compositionKey }
      .collect {
        updatedOnValueChange(it.value)
      }
  }

  return remember(compositionKey) {
    FloatValueChangeAction(compositionKey)
  }
}
