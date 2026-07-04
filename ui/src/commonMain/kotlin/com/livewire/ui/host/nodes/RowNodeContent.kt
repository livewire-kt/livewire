package com.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.ui.host.LayoutNodeContent
import com.livewire.ui.host.debugFrame
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.RowNode
import com.livewire.ui.layout.toComposeUi

@Composable
internal fun RowNodeContent(
  node: RowNode,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.debugFrame(),
    horizontalArrangement = node.horizontalArrangement.toComposeUi(),
    verticalAlignment = when (node.verticalAlignment) {
      Alignment.Bottom -> androidx.compose.ui.Alignment.Bottom
      Alignment.CenterVertically -> androidx.compose.ui.Alignment.CenterVertically
      Alignment.Top -> androidx.compose.ui.Alignment.Top
    }
  ) {
    node.children.forEach { child ->
      val modifier = with(child.modifier) { this@Row.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}
