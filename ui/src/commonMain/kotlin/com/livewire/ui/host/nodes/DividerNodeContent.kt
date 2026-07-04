package com.livewire.ui.host.nodes

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.DividerNode
import com.livewire.ui.widget.DividerStyle

@Composable
internal fun DividerNodeContent(
  node: DividerNode,
  modifier: Modifier = Modifier,
) {
  when (node.style) {
    DividerStyle.Horizontal -> HorizontalDivider(
      modifier = modifier.debugFrame(),
      thickness = node.thickness,
    )
    DividerStyle.Vertical -> VerticalDivider(
      modifier = modifier.debugFrame(),
      thickness = node.thickness,
    )
  }
}
