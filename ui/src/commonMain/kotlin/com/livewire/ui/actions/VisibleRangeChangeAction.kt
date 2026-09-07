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

// reports the index range currently on screen. if `last` is -1, nothing is visible.
@Immutable
@Serializable
data class VisibleRangeChangeAction(
  val identifier: String,
  val first: Int = 0,
  val last: Int = -1,
) : LivewireAction

@Composable
fun visibleRangeChangeAction(
  key: Any? = null,
  onVisibleRangeChange: (first: Int, last: Int) -> Unit,
): VisibleRangeChangeAction {
  val compositionKey = "visibleRangeChange_${key ?: currentCompositeKeyHashCode}"
  val updatedOnVisibleRangeChange by rememberUpdatedState(onVisibleRangeChange)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<VisibleRangeChangeAction>()
      .filter { it.identifier == compositionKey }
      .collect {
        updatedOnVisibleRangeChange(it.first, it.last)
      }
  }

  return remember(compositionKey) {
    VisibleRangeChangeAction(compositionKey)
  }
}
