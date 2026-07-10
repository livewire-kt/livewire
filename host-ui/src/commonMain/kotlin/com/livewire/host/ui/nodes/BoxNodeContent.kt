package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.debugFrame
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.BoxNode

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
      key(child.compositeKeyHash) {
        val modifier = with(child.modifier) { this@Box.toComposeUi(Modifier) }
        LayoutNodeContent(child, modifier)
      }
    }
  }
}
