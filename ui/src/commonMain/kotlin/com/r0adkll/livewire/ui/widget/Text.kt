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
fun Text(
  text: String,
  modifier: LivewireModifier = LivewireModifier,
  style: TextStyle? = null,
  fontWeight: Int? = null,
) {
  ReusableComposeNode<TextNode, Applier<LayoutNode>>(
    factory = { TextNode(text) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(text, TextNode.SetText)
      set(style, TextNode.SetStyle)
      set(fontWeight, TextNode.SetFontWeight)
    },
  )
}

@Serializable
class TextNode(
  var text: String,
  var style: TextStyle? = null,
  var fontWeight: Int? = null,
) : LayoutNode() {

  companion object {
    val SetText: TextNode.(String) -> Unit = applier { text = it }
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
