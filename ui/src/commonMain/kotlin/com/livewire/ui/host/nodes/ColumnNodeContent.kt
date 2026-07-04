package com.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.livewire.ui.host.LayoutNodeContent
import com.livewire.ui.host.debugFrame
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.ColumnNode
import com.livewire.ui.layout.toComposeUi

@Composable
internal fun ColumnNodeContent(
  node: ColumnNode,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.debugFrame(),
    verticalArrangement = node.verticalArrangement.toComposeUi(),
    horizontalAlignment = when (node.horizontalAlignment) {
      Alignment.CenterHorizontally -> androidx.compose.ui.Alignment.CenterHorizontally
      Alignment.End -> androidx.compose.ui.Alignment.End
      Alignment.Start -> androidx.compose.ui.Alignment.Start
    }
  ) {
    node.children.forEach { child ->
      val modifier = with(child.modifier) { this@Column.toComposeUi(Modifier) }
      LayoutNodeContent(child, modifier)
    }
  }
}
