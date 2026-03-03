package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.ui.graphics.Color
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Text(
  text: String,
  modifier: LivewireModifier = LivewireModifier,
  color: Color = Color.Unspecified,
  style: TextStyle? = null,
  fontWeight: Int? = null,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<TextNode, Applier<LayoutNode>>(
    factory = { TextNode(text) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(color, TextNode.SetColor)
      set(text, TextNode.SetText)
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
  var style: TextStyle? = null,
  var fontWeight: Int? = null,
) : LayoutNode() {

  override fun shallowCopy(): TextNode = TextNode(text, color, style, fontWeight)

  companion object {
    val SetText: TextNode.(String) -> Unit = applier { text = it }
    val SetColor: TextNode.(Color) -> Unit = applier { color = it }
    val SetStyle: TextNode.(TextStyle?) -> Unit = applier { style = it }
    val SetFontWeight: TextNode.(Int?) -> Unit = applier { fontWeight = it }
  }
}

enum class TextStyle {
  DisplayLarge,
  DisplayMedium,
  DisplaySmall,
  HeadlineLarge,
  HeadlineMedium,
  HeadlineSmall,
  TitleLarge,
  TitleMedium,
  TitleSmall,
  BodyLarge,
  BodyMedium,
  BodySmall,
  LabelLarge,
  LabelMedium,
  LabelSmall,
}
