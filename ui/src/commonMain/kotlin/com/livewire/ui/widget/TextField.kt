package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
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
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
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
