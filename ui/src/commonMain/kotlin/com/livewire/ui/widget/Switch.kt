package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.CheckedChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
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
    val SetChecked: SwitchNode.(Boolean) -> Unit = applier { checked = it }
    val SetCheckedChange: SwitchNode.(CheckedChangeAction) -> Unit = applier { onCheckedChange = it }
    val SetEnabled: SwitchNode.(Boolean) -> Unit = applier { enabled = it }
  }
}
