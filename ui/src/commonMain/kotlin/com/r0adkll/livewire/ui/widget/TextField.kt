package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.actions.ValueChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun TextField(
  initialValue: String,
  onValueChange: ValueChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  label: String? = null,
  placeholder: String? = null,
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  style: TextFieldStyle = TextFieldStyle.Filled,
) {
  ReusableComposeNode<TextFieldNode, Applier<LayoutNode>>(
    factory = { TextFieldNode(initialValue, onValueChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(initialValue, TextFieldNode.SetInitialValue)
      set(onValueChange, TextFieldNode.SetOnValueChange)
      set(enabled, TextFieldNode.SetEnabled)
      set(readOnly, TextFieldNode.SetReadOnly)
      set(label, TextFieldNode.SetLabel)
      set(placeholder, TextFieldNode.SetPlaceholder)
      set(singleLine, TextFieldNode.SetSingleLine)
      set(maxLines, TextFieldNode.SetMaxLines)
      set(style, TextFieldNode.SetStyle)
    },
  )
}

@LivewireLayoutNode
@Serializable
class TextFieldNode(
  var initialValue: String,
  var onValueChange: ValueChangeAction,
  var enabled: Boolean = true,
  var readOnly: Boolean = false,
  var label: String? = null,
  var placeholder: String? = null,
  var singleLine: Boolean = false,
  var maxLines: Int = Int.MAX_VALUE,
  var style: TextFieldStyle = TextFieldStyle.Filled,
) : LayoutNode() {

  companion object {
    val SetInitialValue: TextFieldNode.(String) -> Unit = applier { initialValue = it }
    val SetOnValueChange: TextFieldNode.(ValueChangeAction) -> Unit = applier { onValueChange = it }
    val SetEnabled: TextFieldNode.(Boolean) -> Unit = applier { enabled = it }
    val SetReadOnly: TextFieldNode.(Boolean) -> Unit = applier { readOnly = it }
    val SetLabel: TextFieldNode.(String?) -> Unit = applier { label = it }
    val SetPlaceholder: TextFieldNode.(String?) -> Unit = applier { placeholder = it }
    val SetSingleLine: TextFieldNode.(Boolean) -> Unit = applier { singleLine = it }
    val SetMaxLines: TextFieldNode.(Int) -> Unit = applier { maxLines = it }
    val SetStyle: TextFieldNode.(TextFieldStyle) -> Unit = applier { style = it }
  }
}

@Serializable
enum class TextFieldStyle {
  Filled,
  Outlined,
}
