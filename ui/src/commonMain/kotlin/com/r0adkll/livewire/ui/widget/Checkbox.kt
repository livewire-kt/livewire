package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.CheckedChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
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
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<CheckboxNode, Applier<LayoutNode>>(
    factory = { CheckboxNode(checked, onCheckedChange, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(checked, CheckboxNode.SetChecked)
      set(onCheckedChange, CheckboxNode.SetCheckedChange)
      set(enabled, CheckboxNode.SetEnabled)
    }
  )
}

@LivewireSerializer
@Serializable
class CheckboxNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean,
) : LayoutNode() {

  companion object {
    val SetChecked: CheckboxNode.(Boolean) -> Unit = { checked = it }
    val SetCheckedChange: CheckboxNode.(CheckedChangeAction) -> Unit = { onCheckedChange = it }
    val SetEnabled: CheckboxNode.(Boolean) -> Unit = { enabled = it }
  }
}
