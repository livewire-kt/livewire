package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.Color
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun BasicTextField(
  initialValue: String,
  onValueChange: ValueChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle? = null,
  textColor: Color = Color.Unspecified,
  fontWeight: Int? = null,
  cursorColor: Color = Color.Unspecified,
  placeholder: String? = null,
  singleLine: Boolean = false,
  minLines: Int = 1,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<BasicTextFieldNode, Applier<LayoutNode>>(
    factory = { BasicTextFieldNode(initialValue, onValueChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(initialValue, BasicTextFieldNode.SetInitialValue)
      update(onValueChange, BasicTextFieldNode.SetOnValueChange)
      set(enabled, BasicTextFieldNode.SetEnabled)
      set(readOnly, BasicTextFieldNode.SetReadOnly)
      set(textStyle, BasicTextFieldNode.SetTextStyle)
      set(textColor, BasicTextFieldNode.SetTextColor)
      set(fontWeight, BasicTextFieldNode.SetFontWeight)
      set(cursorColor, BasicTextFieldNode.SetCursorColor)
      set(placeholder, BasicTextFieldNode.SetPlaceholder)
      set(singleLine, BasicTextFieldNode.SetSingleLine)
      set(minLines, BasicTextFieldNode.SetMinLines)
      set(maxLines, BasicTextFieldNode.SetMaxLines)
    },
  )
}

@LivewireSerializer
@Serializable
class BasicTextFieldNode(
  var initialValue: String,
  var onValueChange: ValueChangeAction,
  var enabled: Boolean = true,
  var readOnly: Boolean = false,
  var textStyle: TextStyle? = null,
  @Serializable(with = ColorSerializer::class)
  var textColor: Color = Color.Unspecified,
  var fontWeight: Int? = null,
  @Serializable(with = ColorSerializer::class)
  var cursorColor: Color = Color.Unspecified,
  var placeholder: String? = null,
  var singleLine: Boolean = false,
  var minLines: Int = 1,
  var maxLines: Int = Int.MAX_VALUE,
) : LayoutNode() {

  companion object {
    val SetInitialValue: BasicTextFieldNode.(String) -> Unit = applier { initialValue = it }
    val SetOnValueChange: BasicTextFieldNode.(ValueChangeAction) -> Unit = applier { onValueChange = it }
    val SetEnabled: BasicTextFieldNode.(Boolean) -> Unit = applier { enabled = it }
    val SetReadOnly: BasicTextFieldNode.(Boolean) -> Unit = applier { readOnly = it }
    val SetTextStyle: BasicTextFieldNode.(TextStyle?) -> Unit = applier { textStyle = it }
    val SetTextColor: BasicTextFieldNode.(Color) -> Unit = applier { textColor = it }
    val SetFontWeight: BasicTextFieldNode.(Int?) -> Unit = applier { fontWeight = it }
    val SetCursorColor: BasicTextFieldNode.(Color) -> Unit = applier { cursorColor = it }
    val SetPlaceholder: BasicTextFieldNode.(String?) -> Unit = applier { placeholder = it }
    val SetSingleLine: BasicTextFieldNode.(Boolean) -> Unit = applier { singleLine = it }
    val SetMinLines: BasicTextFieldNode.(Int) -> Unit = applier { minLines = it }
    val SetMaxLines: BasicTextFieldNode.(Int) -> Unit = applier { maxLines = it }
  }
}
