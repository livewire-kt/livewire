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
data class CheckedChangeAction(
  val identifier: String,
  val checked: Boolean = false,
) : LivewireAction

@Composable
fun checkedChangeAction(
  onCheckedChange: (Boolean) -> Unit,
): CheckedChangeAction {
  val compositionKey = "onCheckedChange_${currentCompositeKeyHashCode}"
  val updatedOnCheckedChange by rememberUpdatedState(onCheckedChange)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<CheckedChangeAction>()
      .filter { it.identifier == compositionKey }
      .collect {
        updatedOnCheckedChange(it.checked)
      }
  }

  return remember(compositionKey) {
    CheckedChangeAction(compositionKey)
  }
}
