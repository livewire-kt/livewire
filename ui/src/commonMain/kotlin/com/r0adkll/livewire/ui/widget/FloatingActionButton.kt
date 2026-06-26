package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.layout.RowScopeInstance
import com.r0adkll.livewire.ui.modifier.LivewireModifier
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
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
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
    val SetAction: FloatingActionButtonNode.(ClickAction) -> Unit = { action = it }
    val SetSize: FloatingActionButtonNode.(FabSize) -> Unit = { size = it }
    val SetStyle: FloatingActionButtonNode.(FabStyle) -> Unit = { style = it }
    val SetExpanded: FloatingActionButtonNode.(Boolean) -> Unit = { expanded = it }
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
