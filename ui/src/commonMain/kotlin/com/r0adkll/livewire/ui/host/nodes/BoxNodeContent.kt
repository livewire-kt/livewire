package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.BoxNode

@Composable
internal fun BoxNodeContent(
  node: BoxNode,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.debugFrame(),
    contentAlignment = when (node.contentAlignment) {
      Alignment.BottomCenter -> androidx.compose.ui.Alignment.BottomCenter
      Alignment.BottomEnd -> androidx.compose.ui.Alignment.BottomEnd
      Alignment.BottomStart -> androidx.compose.ui.Alignment.BottomStart
      Alignment.Center -> androidx.compose.ui.Alignment.Center
      Alignment.CenterEnd -> androidx.compose.ui.Alignment.CenterEnd
      Alignment.CenterStart -> androidx.compose.ui.Alignment.CenterStart
      else -> androidx.compose.ui.Alignment.TopStart
    }
  ) {
    node.children.forEach { child ->
      val modifier = with(child.modifier) { this@Box.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}
