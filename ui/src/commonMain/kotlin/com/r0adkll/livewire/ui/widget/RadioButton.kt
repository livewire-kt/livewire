package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun RadioButton(
  selected: Boolean,
  onClick: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  ReusableComposeNode<RadioButtonNode, Applier<LayoutNode>>(
    factory = { RadioButtonNode(selected, onClick) },
    update = {
      set(modifier, LayoutNode.SetModifier)
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
