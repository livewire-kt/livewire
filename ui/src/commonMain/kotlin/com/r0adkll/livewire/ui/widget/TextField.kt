package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ValueChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
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
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<TextFieldNode, Applier<LayoutNode>>(
    factory = { TextFieldNode(initialValue, onValueChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(initialValue, TextFieldNode.SetInitialValue)
      update(onValueChange, TextFieldNode.SetOnValueChange)
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

@LivewireSerializer
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
    val SetInitialValue: TextFieldNode.(String) -> Unit = { initialValue = it }
    val SetOnValueChange: TextFieldNode.(ValueChangeAction) -> Unit = { onValueChange = it }
    val SetEnabled: TextFieldNode.(Boolean) -> Unit = { enabled = it }
    val SetReadOnly: TextFieldNode.(Boolean) -> Unit = { readOnly = it }
    val SetLabel: TextFieldNode.(String?) -> Unit = { label = it }
    val SetPlaceholder: TextFieldNode.(String?) -> Unit = { placeholder = it }
    val SetSingleLine: TextFieldNode.(Boolean) -> Unit = { singleLine = it }
    val SetMaxLines: TextFieldNode.(Int) -> Unit = { maxLines = it }
    val SetStyle: TextFieldNode.(TextFieldStyle) -> Unit = { style = it }
  }
}

@Serializable
enum class TextFieldStyle {
  Filled,
  Outlined,
}
