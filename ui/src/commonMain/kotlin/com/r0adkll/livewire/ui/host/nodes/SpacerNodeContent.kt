package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.SpacerNode

@Composable
internal fun SpacerNodeContent(
  node: SpacerNode,
  modifier: Modifier = Modifier,
) {
  Spacer(
    modifier = node.modifier.toComposeUi(modifier)
  )
}
