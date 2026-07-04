package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowScope
import com.livewire.ui.layout.RowScopeInstance
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun FloatingActionButton(
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  size: FabSize = FabSize.Default,
  style: FabStyle = FabStyle.Primary,
  expanded: Boolean = true,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<FloatingActionButtonNode, Applier<LayoutNode>>(
    factory = { FloatingActionButtonNode(action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(action, FloatingActionButtonNode.SetAction)
      set(size, FloatingActionButtonNode.SetSize)
      set(style, FloatingActionButtonNode.SetStyle)
      set(expanded, FloatingActionButtonNode.SetExpanded)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class FloatingActionButtonNode(
  var action: ClickAction,
  var size: FabSize = FabSize.Default,
  var style: FabStyle = FabStyle.Primary,
  var expanded: Boolean = true,
) : LayoutNode() {

  companion object {
    val SetAction: FloatingActionButtonNode.(ClickAction) -> Unit = applier { action = it }
    val SetSize: FloatingActionButtonNode.(FabSize) -> Unit = applier { size = it }
    val SetStyle: FloatingActionButtonNode.(FabStyle) -> Unit = applier { style = it }
    val SetExpanded: FloatingActionButtonNode.(Boolean) -> Unit = applier { expanded = it }
  }
}

@Serializable
enum class FabSize {
  Small,
  Default,
  Large,
}

@Serializable
enum class FabStyle {
  Primary,
  Surface,
  Secondary,
  Tertiary,
}
