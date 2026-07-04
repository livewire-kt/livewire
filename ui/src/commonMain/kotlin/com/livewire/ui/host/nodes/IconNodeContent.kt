package com.livewire.ui.host.nodes

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.livewire.ui.host.RemoteIcon
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.IconNode

@Composable
internal fun IconNodeContent(
  node: IconNode,
  modifier: Modifier = Modifier,
) {
  RemoteIcon(
    svgData = node.svgData,
    contentDescription = null,
    tint = node.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
    modifier = modifier.debugFrame(),
  )
}
