package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.DividerNode
import com.r0adkll.livewire.ui.widget.DividerStyle

@Composable
internal fun DividerNodeContent(
  node: DividerNode,
  modifier: Modifier = Modifier,
) {
  when (node.style) {
    DividerStyle.Horizontal -> HorizontalDivider(
      modifier = modifier.debugFrame(),
      thickness = node.thickness.dp,
    )
    DividerStyle.Vertical -> VerticalDivider(
      modifier = modifier.debugFrame(),
      thickness = node.thickness.dp,
    )
  }
}
