package com.r0adkll.livewire.ui.widget

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.ui.graphics.Color
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Icon(
  svgData: String,
  modifier: LivewireModifier = LivewireModifier,
  tint: Color = Color.Unspecified,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<IconNode, Applier<LayoutNode>>(
    factory = { IconNode(svgData) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(svgData, IconNode.SetSvgData)
      set(tint, IconNode.SetTint)
    }
  )
}

@LivewireSerializer
@Serializable
class IconNode(
  var svgData: String,
) : LayoutNode() {
  @Serializable(with = ColorSerializer::class)
  var tint: Color = Color.Unspecified

  companion object {
    val SetSvgData: IconNode.(String) -> Unit = { svgData = it }
    val SetTint: IconNode.(Color) -> Unit = applier { tint = it }
  }
}
