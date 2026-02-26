package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.graphics.RectangleShape
import com.r0adkll.livewire.ui.graphics.Shape
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Surface(
  modifier: LivewireModifier = LivewireModifier,
  shape: Shape = RectangleShape,
  color: Int? = null,
  tonalElevation: Float = 0f,
  shadowElevation: Float = 0f,
  onClick: ClickAction? = null,
  content: @Composable @LivewireComposable () -> Unit,
) {
  ReusableComposeNode<SurfaceNode, Applier<LayoutNode>>(
    factory = { SurfaceNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(shape, SurfaceNode.SetShape)
      set(color, SurfaceNode.SetColor)
      set(tonalElevation, SurfaceNode.SetTonalElevation)
      set(shadowElevation, SurfaceNode.SetShadowElevation)
      set(onClick, SurfaceNode.SetOnClick)
    },
    content = { content() },
  )
}

@LivewireLayoutNode
@Serializable
class SurfaceNode(
  var shape: Shape = RectangleShape,
  var color: Int? = null,
  var tonalElevation: Float = 0f,
  var shadowElevation: Float = 0f,
  var onClick: ClickAction? = null,
) : LayoutNode() {

  companion object {
    val SetShape: SurfaceNode.(Shape) -> Unit = applier { shape = it }
    val SetColor: SurfaceNode.(Int?) -> Unit = applier { color = it }
    val SetTonalElevation: SurfaceNode.(Float) -> Unit = applier { tonalElevation = it }
    val SetShadowElevation: SurfaceNode.(Float) -> Unit = applier { shadowElevation = it }
    val SetOnClick: SurfaceNode.(ClickAction?) -> Unit = applier { onClick = it }
  }
}
