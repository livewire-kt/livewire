package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun IconButton(
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  size: ButtonSize = ButtonSize.Small,
  style: IconButtonStyle = IconButtonStyle.Default,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<IconButtonNode, Applier<LayoutNode>>(
    factory = { IconButtonNode(action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(action, IconButtonNode.SetAction)
      set(enabled, IconButtonNode.SetEnabled)
      set(size, IconButtonNode.SetSize)
      set(style, IconButtonNode.SetStyle)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class IconButtonNode(
  var action: ClickAction,
  var enabled: Boolean = true,
  var size: ButtonSize = ButtonSize.Small,
  var style: IconButtonStyle = IconButtonStyle.Default,
) : LayoutNode() {

  companion object {
    val SetAction: IconButtonNode.(ClickAction) -> Unit = { action = it }
    val SetEnabled: IconButtonNode.(Boolean) -> Unit = { enabled = it }
    val SetSize: IconButtonNode.(ButtonSize) -> Unit = { size = it }
    val SetStyle: IconButtonNode.(IconButtonStyle) -> Unit = { style = it }
  }
}

enum class IconButtonStyle {
  Default,
  Filled,
  Tonal,
  Outlined,
}
