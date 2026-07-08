package com.livewire.ui.host.nodes

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.livewire.ui.host.LayoutNodeContent
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.FloatingToolbarNode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FloatingToolbarNodeContent(
  node: FloatingToolbarNode,
  modifier: Modifier = Modifier,
) {
  HorizontalFloatingToolbar(
    expanded = node.expanded,
    modifier = modifier.debugFrame(),
    content = {
      node.children.forEach { child ->
        key(child.compositeKeyHash) {
          LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
        }
      }
    },
  )
}
