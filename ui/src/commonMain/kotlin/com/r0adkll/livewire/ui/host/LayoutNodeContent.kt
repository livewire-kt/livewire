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
import com.r0adkll.livewire.ui.host.nodes.CheckboxNodeContent
import com.r0adkll.livewire.ui.host.nodes.DropdownMenuNodeContent
import com.r0adkll.livewire.ui.host.nodes.ColumnNodeContent
import com.r0adkll.livewire.ui.host.nodes.FloatingActionButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.IconButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.RowNodeContent
import com.r0adkll.livewire.ui.host.nodes.SurfaceNodeContent
import com.r0adkll.livewire.ui.host.nodes.TextNodeContent
import com.r0adkll.livewire.ui.host.nodes.ProgressIndicatorNodeContent
import com.r0adkll.livewire.ui.host.nodes.RadioButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.TextFieldNodeContent
import com.r0adkll.livewire.ui.host.nodes.ToggleButtonNodeContent
import com.r0adkll.livewire.ui.layout.BoxNode
import com.r0adkll.livewire.ui.layout.ColumnNode
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowNode
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.CheckboxNode
import com.r0adkll.livewire.ui.widget.DropdownMenuNode
import com.r0adkll.livewire.ui.widget.FloatingActionButtonNode
import com.r0adkll.livewire.ui.widget.IconButtonNode
import com.r0adkll.livewire.ui.widget.TextNode
import com.r0adkll.livewire.ui.widget.ProgressIndicatorNode
import com.r0adkll.livewire.ui.widget.RadioButtonNode
import com.r0adkll.livewire.ui.widget.SurfaceNode
import com.r0adkll.livewire.ui.widget.TextFieldNode
import com.r0adkll.livewire.ui.widget.ToggleButtonNode

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
    is CheckboxNode -> CheckboxNodeContent(node, modifier)
    is DropdownMenuNode -> DropdownMenuNodeContent(node, modifier)
    is FloatingActionButtonNode -> FloatingActionButtonNodeContent(node, modifier)
    is ProgressIndicatorNode -> ProgressIndicatorNodeContent(node, modifier)
    is RadioButtonNode -> RadioButtonNodeContent(node, modifier)
    is SurfaceNode -> SurfaceNodeContent(node, modifier)
    is TextFieldNode -> TextFieldNodeContent(node, modifier)
    is ToggleButtonNode -> ToggleButtonNodeContent(node, modifier)

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
