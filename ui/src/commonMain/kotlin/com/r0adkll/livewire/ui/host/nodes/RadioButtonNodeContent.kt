package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.RadioButtonNode
import kotlinx.coroutines.launch

@Composable
internal fun RadioButtonNodeContent(
  node: RadioButtonNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  RadioButton(
    selected = node.selected,
    onClick = {
      scope.launch {
        eventDispatcher.dispatch(node.onClick)
      }
    },
    modifier = modifier.debugFrame(),
    enabled = node.enabled,
  )
}
