package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.SliderNode
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
