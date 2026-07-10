package com.livewire.host.ui.nodes

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.SliderNode
import kotlinx.coroutines.launch

@Composable
internal fun SliderNodeContent(
  node: SliderNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  Slider(
    value = node.value,
    onValueChange = {
      scope.launch {
        eventDispatcher.dispatch(node.onValueChange.copy(value = it))
      }
    },
    modifier = modifier.debugFrame(),
    enabled = node.enabled,
    valueRange = node.valueRangeStart..node.valueRangeEnd,
    steps = node.steps,
  )
}
