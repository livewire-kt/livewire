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
fun Chip(
  label: String,
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  style: ChipStyle = ChipStyle.Assist,
  elevated: Boolean = false,
  selected: Boolean = false,
  enabled: Boolean = true,
  leadingIconData: String? = null,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ChipNode, Applier<LayoutNode>>(
    factory = { ChipNode(label, action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(label, ChipNode.SetLabel)
      update(action, ChipNode.SetAction)
      set(style, ChipNode.SetStyle)
      set(elevated, ChipNode.SetElevated)
      set(selected, ChipNode.SetSelected)
      set(enabled, ChipNode.SetEnabled)
      set(leadingIconData, ChipNode.SetLeadingIconData)
    },
  )
}

@LivewireSerializer
@Serializable
class ChipNode(
  var label: String,
  var action: ClickAction,
  var style: ChipStyle = ChipStyle.Assist,
  var elevated: Boolean = false,
  var selected: Boolean = false,
  var enabled: Boolean = true,
  var leadingIconData: String? = null,
) : LayoutNode() {

  companion object {
    val SetLabel: ChipNode.(String) -> Unit = applier { label = it }
    val SetAction: ChipNode.(ClickAction) -> Unit = applier { action = it }
    val SetStyle: ChipNode.(ChipStyle) -> Unit = applier { style = it }
    val SetElevated: ChipNode.(Boolean) -> Unit = applier { elevated = it }
    val SetSelected: ChipNode.(Boolean) -> Unit = applier { selected = it }
    val SetEnabled: ChipNode.(Boolean) -> Unit = applier { enabled = it }
    val SetLeadingIconData: ChipNode.(String?) -> Unit = applier { leadingIconData = it }
  }
}

@Serializable
enum class ChipStyle {
  Assist,
  Filter,
  Input,
  Suggestion,
}
