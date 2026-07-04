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
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun RadioButton(
  selected: Boolean,
  onClick: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<RadioButtonNode, Applier<LayoutNode>>(
    factory = { RadioButtonNode(selected, onClick) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(selected, RadioButtonNode.SetSelected)
      set(onClick, RadioButtonNode.SetOnClick)
      set(enabled, RadioButtonNode.SetEnabled)
    },
  )
}

@LivewireSerializer
@Serializable
class RadioButtonNode(
  var selected: Boolean,
  var onClick: ClickAction,
  var enabled: Boolean = true,
) : LayoutNode() {

  companion object {
    val SetSelected: RadioButtonNode.(Boolean) -> Unit = applier { selected = it }
    val SetOnClick: RadioButtonNode.(ClickAction) -> Unit = applier { onClick = it }
    val SetEnabled: RadioButtonNode.(Boolean) -> Unit = applier { enabled = it }
  }
}
