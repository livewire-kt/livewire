package com.r0adkll.livewire.ui.host

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.host.nodes.AnimatedVisibilityNodeContent
import com.r0adkll.livewire.ui.host.nodes.BoxNodeContent
import com.r0adkll.livewire.ui.host.nodes.ButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.CheckboxNodeContent
import com.r0adkll.livewire.ui.host.nodes.ChipNodeContent
import com.r0adkll.livewire.ui.host.nodes.ColumnNodeContent
import com.r0adkll.livewire.ui.host.nodes.DividerNodeContent
import com.r0adkll.livewire.ui.host.nodes.DropdownMenuNodeContent
import com.r0adkll.livewire.ui.host.nodes.FloatingActionButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.FloatingToolbarNodeContent
import com.r0adkll.livewire.ui.host.nodes.IconButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.IconNodeContent
import com.r0adkll.livewire.ui.host.nodes.ImageNodeContent
import com.r0adkll.livewire.ui.host.nodes.ProgressIndicatorNodeContent
import com.r0adkll.livewire.ui.host.nodes.RadioButtonNodeContent
import com.r0adkll.livewire.ui.host.nodes.ResizableSurfaceNodeContent
import com.r0adkll.livewire.ui.host.nodes.RowNodeContent
import com.r0adkll.livewire.ui.host.nodes.SliderNodeContent
import com.r0adkll.livewire.ui.host.nodes.SpacerNodeContent
import com.r0adkll.livewire.ui.host.nodes.SurfaceNodeContent
import com.r0adkll.livewire.ui.host.nodes.SwitchNodeContent
import com.r0adkll.livewire.ui.host.nodes.TextFieldNodeContent
import com.r0adkll.livewire.ui.host.nodes.TextNodeContent
import com.r0adkll.livewire.ui.host.nodes.TableNodeContent
import com.r0adkll.livewire.ui.host.nodes.TabRowNodeContent
import com.r0adkll.livewire.ui.host.nodes.ToggleButtonNodeContent
import com.r0adkll.livewire.ui.layout.BoxNode
import com.r0adkll.livewire.ui.layout.ColumnNode
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowNode
import com.r0adkll.livewire.ui.widget.AnimatedVisibilityNode
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.CheckboxNode
import com.r0adkll.livewire.ui.widget.ChipNode
import com.r0adkll.livewire.ui.widget.DividerNode
import com.r0adkll.livewire.ui.widget.DropdownMenuNode
import com.r0adkll.livewire.ui.widget.FloatingActionButtonNode
import com.r0adkll.livewire.ui.widget.FloatingToolbarNode
import com.r0adkll.livewire.ui.widget.IconButtonNode
import com.r0adkll.livewire.ui.widget.IconNode
import com.r0adkll.livewire.ui.widget.ImageNode
import com.r0adkll.livewire.ui.widget.ProgressIndicatorNode
import com.r0adkll.livewire.ui.widget.RadioButtonNode
import com.r0adkll.livewire.ui.widget.ResizableSurfaceNode
import com.r0adkll.livewire.ui.widget.SliderNode
import com.r0adkll.livewire.ui.widget.SpacerNode
import com.r0adkll.livewire.ui.widget.SurfaceNode
import com.r0adkll.livewire.ui.widget.SwitchNode
import com.r0adkll.livewire.ui.widget.TextFieldNode
import com.r0adkll.livewire.ui.widget.TextNode
import com.r0adkll.livewire.ui.widget.TableNode
import com.r0adkll.livewire.ui.widget.TabRowNode
import com.r0adkll.livewire.ui.widget.ToggleButtonNode

@Composable
fun LayoutNodeContent(
  node: LayoutNode,
  modifier: Modifier,
) {
  when (node) {
    is AnimatedVisibilityNode -> AnimatedVisibilityNodeContent(node, modifier)
    is BoxNode -> BoxNodeContent(node, modifier)
    is ColumnNode -> ColumnNodeContent(node, modifier)
    is RowNode -> RowNodeContent(node, modifier)
    is TextNode -> TextNodeContent(node, modifier)
    is ButtonNode -> ButtonNodeContent(node, modifier)
    is IconButtonNode -> IconButtonNodeContent(node, modifier)
    is IconNode -> IconNodeContent(node, modifier)
    is ImageNode -> ImageNodeContent(node, modifier)
    is CheckboxNode -> CheckboxNodeContent(node, modifier)
    is ChipNode -> ChipNodeContent(node, modifier)
    is DividerNode -> DividerNodeContent(node, modifier)
    is DropdownMenuNode -> DropdownMenuNodeContent(node, modifier)
    is FloatingActionButtonNode -> FloatingActionButtonNodeContent(node, modifier)
    is FloatingToolbarNode -> FloatingToolbarNodeContent(node, modifier)
    is ProgressIndicatorNode -> ProgressIndicatorNodeContent(node, modifier)
    is RadioButtonNode -> RadioButtonNodeContent(node, modifier)
    is ResizableSurfaceNode -> ResizableSurfaceNodeContent(node, modifier)
    is SliderNode -> SliderNodeContent(node, modifier)
    is SpacerNode -> SpacerNodeContent(node, modifier)
    is SurfaceNode -> SurfaceNodeContent(node, modifier)
    is SwitchNode -> SwitchNodeContent(node, modifier)
    is TextFieldNode -> TextFieldNodeContent(node, modifier)
    is TableNode -> TableNodeContent(node, modifier)
    is TabRowNode -> TabRowNodeContent(node, modifier)
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
