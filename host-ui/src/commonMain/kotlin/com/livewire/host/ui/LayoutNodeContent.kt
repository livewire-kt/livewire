package com.livewire.host.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.livewire.host.ui.nodes.AnimatedVisibilityNodeContent
import com.livewire.host.ui.nodes.BasicTextFieldNodeContent
import com.livewire.host.ui.nodes.BoxNodeContent
import com.livewire.host.ui.nodes.ButtonNodeContent
import com.livewire.host.ui.nodes.CheckboxNodeContent
import com.livewire.host.ui.nodes.ChipNodeContent
import com.livewire.host.ui.nodes.CodeBlockNodeContent
import com.livewire.host.ui.nodes.ColumnNodeContent
import com.livewire.host.ui.nodes.DividerNodeContent
import com.livewire.host.ui.nodes.DropdownMenuNodeContent
import com.livewire.host.ui.nodes.FloatingActionButtonNodeContent
import com.livewire.host.ui.nodes.FloatingToolbarNodeContent
import com.livewire.host.ui.nodes.IconButtonNodeContent
import com.livewire.host.ui.nodes.IconNodeContent
import com.livewire.host.ui.nodes.ImageNodeContent
import com.livewire.host.ui.nodes.ProgressIndicatorNodeContent
import com.livewire.host.ui.nodes.RadioButtonNodeContent
import com.livewire.host.ui.nodes.ResizableSurfaceNodeContent
import com.livewire.host.ui.nodes.RowNodeContent
import com.livewire.host.ui.nodes.SliderNodeContent
import com.livewire.host.ui.nodes.SpacerNodeContent
import com.livewire.host.ui.nodes.SurfaceNodeContent
import com.livewire.host.ui.nodes.SwitchNodeContent
import com.livewire.host.ui.nodes.TextFieldNodeContent
import com.livewire.host.ui.nodes.TextNodeContent
import com.livewire.host.ui.nodes.TableNodeContent
import com.livewire.host.ui.nodes.TabRowNodeContent
import com.livewire.host.ui.nodes.ToggleButtonNodeContent
import com.livewire.ui.layout.BoxNode
import com.livewire.ui.layout.ColumnNode
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowNode
import com.livewire.ui.widget.AnimatedVisibilityNode
import com.livewire.ui.widget.BasicTextFieldNode
import com.livewire.ui.widget.ButtonNode
import com.livewire.ui.widget.CheckboxNode
import com.livewire.ui.widget.ChipNode
import com.livewire.ui.widget.CodeBlockNode
import com.livewire.ui.widget.DividerNode
import com.livewire.ui.widget.DropdownMenuNode
import com.livewire.ui.widget.FloatingActionButtonNode
import com.livewire.ui.widget.FloatingToolbarNode
import com.livewire.ui.widget.IconButtonNode
import com.livewire.ui.widget.IconNode
import com.livewire.ui.widget.ImageNode
import com.livewire.ui.widget.ProgressIndicatorNode
import com.livewire.ui.widget.RadioButtonNode
import com.livewire.ui.widget.ResizableSurfaceNode
import com.livewire.ui.widget.SliderNode
import com.livewire.ui.widget.SpacerNode
import com.livewire.ui.widget.SurfaceNode
import com.livewire.ui.widget.SwitchNode
import com.livewire.ui.widget.TextFieldNode
import com.livewire.ui.widget.TextNode
import com.livewire.ui.widget.TableNode
import com.livewire.ui.widget.TabRowNode
import com.livewire.ui.widget.ToggleButtonNode

@Composable
fun LayoutNodeContent(
  node: LayoutNode,
  modifier: Modifier,
) {
  when (node) {
    is AnimatedVisibilityNode -> AnimatedVisibilityNodeContent(node, modifier)
    is BasicTextFieldNode -> BasicTextFieldNodeContent(node, modifier)
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
    is CodeBlockNode -> CodeBlockNodeContent(node, modifier)
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
          key(child.compositeKeyHash) {
            LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
          }
        }
      }
    }
  }
}
