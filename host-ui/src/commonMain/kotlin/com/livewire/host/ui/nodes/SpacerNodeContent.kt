package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.SpacerNode

@Composable
internal fun SpacerNodeContent(
  node: SpacerNode,
  modifier: Modifier = Modifier,
) {
  Spacer(
    modifier = node.modifier.toComposeUi(modifier)
  )
}
