package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.Color
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.text.LivewireTextStyle
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Text(
  text: String,
  modifier: LivewireModifier = LivewireModifier,
  color: Color = Color.Unspecified,
  style: LivewireTextStyle? = null,
  fontWeight: Int? = null,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<TextNode, Applier<LayoutNode>>(
    factory = { TextNode(text) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(color, TextNode.SetColor)
      update(text, TextNode.SetText)
      set(style, TextNode.SetStyle)
      set(fontWeight, TextNode.SetFontWeight)
    },
  )
}

@LivewireSerializer
@Serializable
class TextNode(
  var text: String,
  @Serializable(with = ColorSerializer::class)
  var color: Color = Color.Unspecified,
  var style: LivewireTextStyle? = null,
  var fontWeight: Int? = null,
) : LayoutNode() {

  companion object {
    val SetText: TextNode.(String) -> Unit = applier { text = it }
    val SetColor: TextNode.(Color) -> Unit = applier { color = it }
    val SetStyle: TextNode.(LivewireTextStyle?) -> Unit = applier { style = it }
    val SetFontWeight: TextNode.(Int?) -> Unit = applier { fontWeight = it }
  }
}
