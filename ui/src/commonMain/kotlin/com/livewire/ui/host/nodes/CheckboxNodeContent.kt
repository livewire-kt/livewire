package com.livewire.ui.host.nodes

import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.CheckboxNode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CheckboxNodeContent(
  node: CheckboxNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  Checkbox(
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
