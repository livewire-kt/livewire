package com.livewire.ui.host.nodes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.ui.host.RemoteIcon
import com.livewire.ui.host.debugFrame
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
