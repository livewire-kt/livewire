package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.ColumnScope
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.layout.RowScopeInstance
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Button(
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  size: ButtonSize = ButtonSize.Small,
  style: ButtonStyle = ButtonStyle.Filled,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  ReusableComposeNode<ButtonNode, Applier<LayoutNode>>(
    factory = { ButtonNode(action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      update(action, ButtonNode.SetAction)
      set(size, ButtonNode.SetSize)
      set(style, ButtonNode.SetStyle)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireLayoutNode
@Serializable
class ButtonNode(
  var action: ClickAction,
  var size: ButtonSize = ButtonSize.Small,
  var style: ButtonStyle = ButtonStyle.Filled,
) : LayoutNode() {

  companion object {
    val SetAction: ButtonNode.(ClickAction) -> Unit = applier { action = it }
    val SetSize: ButtonNode.(ButtonSize) -> Unit = applier { size = it }
    val SetStyle: ButtonNode.(ButtonStyle) -> Unit = applier { style = it }
  }
}

enum class ButtonSize {
  ExtraSmall,
  Small,
  Medium,
  Large,
}

enum class ButtonStyle {
  Filled,
  Tonal,
  Outlined,
  Elevated,
  Text,
}
