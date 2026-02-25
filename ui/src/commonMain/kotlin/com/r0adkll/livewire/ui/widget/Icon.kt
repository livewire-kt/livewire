package com.r0adkll.livewire.ui.widget

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Icon(
  svgData: String,
  modifier: LivewireModifier = LivewireModifier,
  tint: Color = Color.Unspecified,
) {
  ReusableComposeNode<IconNode, Applier<LayoutNode>>(
    factory = { IconNode(svgData) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(svgData, IconNode.SetSvgData)
      set(tint, IconNode.SetTint)
    }
  )
}

@Serializable
class IconNode(
  var svgData: String,
) : LayoutNode() {
  var tint: Int = -1

  val tintAsComposeColor: Color
    @Composable get() = tint
      .takeIf { it != -1 }
      ?.let { Color(it) }
      ?: LocalContentColor.current

  companion object {
    val SetSvgData: IconNode.(String) -> Unit = { svgData = it }
    val SetTint: IconNode.(Color) -> Unit = {
      tint = if (it == Color.Unspecified) -1 else it.toArgb()
    }
  }
}
