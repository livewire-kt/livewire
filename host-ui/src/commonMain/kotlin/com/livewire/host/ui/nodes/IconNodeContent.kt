package com.livewire.host.ui.nodes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.host.ui.RemoteIcon
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.IconNode

@Composable
internal fun IconNodeContent(
  node: IconNode,
  modifier: Modifier = Modifier,
) {
  RemoteIcon(
    node = node,
    contentDescription = null,
    modifier = modifier.debugFrame(),
  )
}
