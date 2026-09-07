package com.livewire.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.Serializable

// reports the dp size of a host widget after a user resize
@Immutable
@Serializable
data class SizeChangeAction(
  val identifier: String,
  val size: Float = 0f,
) : LivewireAction

@Composable
fun sizeChangeAction(
  key: Any? = null,
  onSizeChange: (Dp) -> Unit,
): SizeChangeAction {
  val compositionKey = "sizeChange_${key ?: currentCompositeKeyHashCode}"
  val updatedOnSizeChange by rememberUpdatedState(onSizeChange)
  val actionObserver = LocalLivewireActionObserver.current

  LaunchedEffect(compositionKey) {
    actionObserver.events
      .filterIsInstance<SizeChangeAction>()
      .filter { it.identifier == compositionKey }
      .collect { updatedOnSizeChange(it.size.dp) }
  }

  return remember(compositionKey) {
    SizeChangeAction(compositionKey)
  }
}
