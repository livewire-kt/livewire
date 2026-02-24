package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Button(
  text: String,
  action: String,
  modifier: LivewireModifier = LivewireModifier,
  icon: String? = null,
  size: ButtonSize = ButtonSize.Small,
) {
  ReusableComposeNode<ButtonNode, Applier<LayoutNode>>(
    factory = { ButtonNode(text, action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(text, ButtonNode.SetText)
      set(action, ButtonNode.SetActionIntent)
      set(icon, ButtonNode.SetIcon)
      set(size, ButtonNode.SetSize)
    }
  )
}

@Serializable
class ButtonNode(
  var text: String,
  var actionIntent: String,
  var icon: String? = null,
  var size: ButtonSize = ButtonSize.Small,
) : LayoutNode() {

  companion object {
    val SetText: ButtonNode.(String) -> Unit = applier { text = it }
    val SetActionIntent: ButtonNode.(String) -> Unit = applier { actionIntent = it }
    val SetIcon: ButtonNode.(String?) -> Unit = applier { icon = it }
    val SetSize: ButtonNode.(ButtonSize) -> Unit = applier { size = it }
  }
}

enum class ButtonSize {
  ExtraSmall,
  Small,
  Medium,
  Large,
}
