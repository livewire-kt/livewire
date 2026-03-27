package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.CheckedChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Switch(
  checked: Boolean,
  onCheckedChange: CheckedChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<SwitchNode, Applier<LayoutNode>>(
    factory = { SwitchNode(checked, onCheckedChange, enabled) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(checked, SwitchNode.SetChecked)
      set(onCheckedChange, SwitchNode.SetCheckedChange)
      set(enabled, SwitchNode.SetEnabled)
    }
  )
}

@LivewireSerializer
@Serializable
class SwitchNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean,
) : LayoutNode() {

  companion object {
    val SetChecked: SwitchNode.(Boolean) -> Unit = { checked = it }
    val SetCheckedChange: SwitchNode.(CheckedChangeAction) -> Unit = { onCheckedChange = it }
    val SetEnabled: SwitchNode.(Boolean) -> Unit = { enabled = it }
  }
}
