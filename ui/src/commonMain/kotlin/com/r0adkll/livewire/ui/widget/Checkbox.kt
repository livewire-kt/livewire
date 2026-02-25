package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.actions.CheckedChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Checkbox(
  checked: Boolean,
  onCheckedChange: CheckedChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  ReusableComposeNode<CheckboxNode, Applier<LayoutNode>>(
    factory = { CheckboxNode(checked, onCheckedChange, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(checked, CheckboxNode.SetChecked)
      set(onCheckedChange, CheckboxNode.SetCheckedChange)
      set(enabled, CheckboxNode.SetEnabled)
    }
  )
}

@LivewireLayoutNode
@Serializable
class CheckboxNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean,
) : LayoutNode() {

  companion object {
    val SetChecked: CheckboxNode.(Boolean) -> Unit = applier { checked = it }
    val SetCheckedChange: CheckboxNode.(CheckedChangeAction) -> Unit = applier { onCheckedChange = it }
    val SetEnabled: CheckboxNode.(Boolean) -> Unit = applier { enabled = it }
  }
}
