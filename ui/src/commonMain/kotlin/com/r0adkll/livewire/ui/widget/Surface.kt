package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.graphics.RectangleShape
import com.r0adkll.livewire.ui.graphics.Shape
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Surface(
  modifier: LivewireModifier = LivewireModifier,
  shape: Shape = RectangleShape,
  color: @Serializable(with = ColorSerializer::class) Color? = null,
  contentColor: @Serializable(with = ColorSerializer::class) Color? = null,
  tonalElevation: Dp = 0.dp,
  shadowElevation: Dp = 0.dp,
  onClick: ClickAction? = null,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<SurfaceNode, Applier<LayoutNode>>(
    factory = { SurfaceNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(shape, SurfaceNode.SetShape)
      set(color, SurfaceNode.SetColor)
      set(contentColor, SurfaceNode.SetContentColor)
      set(tonalElevation, SurfaceNode.SetTonalElevation)
      set(shadowElevation, SurfaceNode.SetShadowElevation)
      set(onClick, SurfaceNode.SetOnClick)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class SurfaceNode(
  var shape: Shape = RectangleShape,
  @Serializable(with = ColorSerializer::class) var color: Color? = null,
  @Serializable(with = ColorSerializer::class) var contentColor: Color? = null,
  @Serializable(with = DpSerializer::class) var tonalElevation: Dp = 0.dp,
  @Serializable(with = DpSerializer::class) var shadowElevation: Dp = 0.dp,
  var onClick: ClickAction? = null,
) : LayoutNode() {

  override fun shallowCopy(): SurfaceNode = SurfaceNode(shape, color, contentColor, tonalElevation, shadowElevation, onClick)

  companion object {
    val SetShape: SurfaceNode.(Shape) -> Unit = applier { shape = it }
    val SetColor: SurfaceNode.(Color?) -> Unit = applier { color = it }
    val SetContentColor: SurfaceNode.(Color?) -> Unit = applier { contentColor = it }
    val SetTonalElevation: SurfaceNode.(Dp) -> Unit = applier { tonalElevation = it }
    val SetShadowElevation: SurfaceNode.(Dp) -> Unit = applier { shadowElevation = it }
    val SetOnClick: SurfaceNode.(ClickAction?) -> Unit = applier { onClick = it }
  }
}
