package com.r0adkll.livewire.ui.host

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.host.nodes.BoxNodeContent
import com.r0adkll.livewire.ui.host.nodes.ButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.ColumnNodeContent
import com.r0adkll.livewire.ui.host.nodes.IconButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.RowNodeContent
import com.r0adkll.livewire.ui.host.nodes.TextNodeContent
import com.r0adkll.livewire.ui.layout.BoxNode
import com.r0adkll.livewire.ui.layout.ColumnNode
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowNode
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.IconButtonNode
import com.r0adkll.livewire.ui.widget.TextNode

@Composable
fun LayoutNodeContent(
  node: LayoutNode,
  modifier: Modifier,
) {
  when (node) {
    is BoxNode -> BoxNodeContent(node, modifier)
    is ColumnNode -> ColumnNodeContent(node, modifier)
    is RowNode -> RowNodeContent(node, modifier)
    is TextNode -> TextNodeContent(node, modifier)
    is ButtonNode -> ButtonNodeContent(node, modifier)
    is IconButtonNode -> IconButtonNodeContent(node, modifier)

    else -> {
      Box(modifier.debugFrame()) {
        node.children.forEach { child ->
          LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
        }
      }
    }
  }
}

/**
 * Set this to true to draw debugging information on the screen
 */
var DebugNodes by mutableStateOf(false)

internal fun Modifier.debugFrame(): Modifier = if (DebugNodes) {
  border(
    width = 1.dp,
    color = Color.Red
  )
} else this
