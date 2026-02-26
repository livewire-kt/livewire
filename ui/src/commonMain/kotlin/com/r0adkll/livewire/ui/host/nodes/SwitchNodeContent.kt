package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.SwitchNode
import kotlinx.coroutines.launch

@Composable
internal fun SwitchNodeContent(
  node: SwitchNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  Switch(
    checked = node.checked,
    onCheckedChange = {
      scope.launch {
        eventDispatcher.dispatch(node.onCheckedChange.copy(checked = it))
      }
    },
    modifier = modifier.debugFrame(),
    enabled = node.enabled,
  )
}
