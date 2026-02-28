package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.CheckedChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.layout.RowScopeInstance
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun ToggleButton(
  checked: Boolean,
  onCheckedChange: CheckedChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  size: ButtonSize = ButtonSize.Small,
  style: ToggleButtonStyle = ToggleButtonStyle.Filled,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<ToggleButtonNode, Applier<LayoutNode>>(
    factory = { ToggleButtonNode(checked, onCheckedChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(checked, ToggleButtonNode.SetChecked)
      set(onCheckedChange, ToggleButtonNode.SetOnCheckedChange)
      set(enabled, ToggleButtonNode.SetEnabled)
      set(size, ToggleButtonNode.SetSize)
      set(style, ToggleButtonNode.SetStyle)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class ToggleButtonNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean = true,
  var size: ButtonSize = ButtonSize.Small,
  var style: ToggleButtonStyle = ToggleButtonStyle.Filled,
) : LayoutNode() {

  companion object {
    val SetChecked: ToggleButtonNode.(Boolean) -> Unit = applier { checked = it }
    val SetOnCheckedChange: ToggleButtonNode.(CheckedChangeAction) -> Unit = applier { onCheckedChange = it }
    val SetEnabled: ToggleButtonNode.(Boolean) -> Unit = applier { enabled = it }
    val SetSize: ToggleButtonNode.(ButtonSize) -> Unit = applier { size = it }
    val SetStyle: ToggleButtonNode.(ToggleButtonStyle) -> Unit = applier { style = it }
  }
}

@Serializable
enum class ToggleButtonStyle {
  Filled,
  Outlined,
  Elevated,
}
