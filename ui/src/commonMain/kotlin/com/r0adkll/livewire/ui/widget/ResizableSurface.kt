package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.ui.graphics.Color
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.graphics.RectangleShape
import com.r0adkll.livewire.ui.graphics.Shape
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

/**
 * A [Surface] that can be resized by dragging a handle in the bottom-end corner.
 *
 * @param initialWidth initial width in dp
 * @param initialHeight initial height in dp
 * @param minWidth minimum width constraint in dp
 * @param minHeight minimum height constraint in dp
 * @param maxWidth maximum width constraint in dp
 * @param maxHeight maximum height constraint in dp
 */
@LivewireComposable
@Composable
fun ResizableSurface(
  initialWidth: Float,
  initialHeight: Float,
  modifier: LivewireModifier = LivewireModifier,
  minWidth: Float = 50f,
  minHeight: Float = 50f,
  maxWidth: Float = Float.MAX_VALUE,
  maxHeight: Float = Float.MAX_VALUE,
  shape: Shape = RectangleShape,
  color: @Serializable(with = ColorSerializer::class) Color? = null,
  contentColor: @Serializable(with = ColorSerializer::class) Color? = null,
  tonalElevation: Float = 0f,
  shadowElevation: Float = 0f,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<ResizableSurfaceNode, Applier<LayoutNode>>(
    factory = { ResizableSurfaceNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(initialWidth, ResizableSurfaceNode.SetInitialWidth)
      set(initialHeight, ResizableSurfaceNode.SetInitialHeight)
      set(minWidth, ResizableSurfaceNode.SetMinWidth)
      set(minHeight, ResizableSurfaceNode.SetMinHeight)
      set(maxWidth, ResizableSurfaceNode.SetMaxWidth)
      set(maxHeight, ResizableSurfaceNode.SetMaxHeight)
      set(shape, ResizableSurfaceNode.SetShape)
      set(color, ResizableSurfaceNode.SetColor)
      set(contentColor, ResizableSurfaceNode.SetContentColor)
      set(tonalElevation, ResizableSurfaceNode.SetTonalElevation)
      set(shadowElevation, ResizableSurfaceNode.SetShadowElevation)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class ResizableSurfaceNode(
  var initialWidth: Float = 200f,
  var initialHeight: Float = 200f,
  var minWidth: Float = 50f,
  var minHeight: Float = 50f,
  var maxWidth: Float = Float.MAX_VALUE,
  var maxHeight: Float = Float.MAX_VALUE,
  var shape: Shape = RectangleShape,
  @Serializable(with = ColorSerializer::class) var color: Color? = null,
  @Serializable(with = ColorSerializer::class) var contentColor: Color? = null,
  var tonalElevation: Float = 0f,
  var shadowElevation: Float = 0f,
) : LayoutNode() {

  companion object {
    val SetInitialWidth: ResizableSurfaceNode.(Float) -> Unit = applier { initialWidth = it }
    val SetInitialHeight: ResizableSurfaceNode.(Float) -> Unit = applier { initialHeight = it }
    val SetMinWidth: ResizableSurfaceNode.(Float) -> Unit = applier { minWidth = it }
    val SetMinHeight: ResizableSurfaceNode.(Float) -> Unit = applier { minHeight = it }
    val SetMaxWidth: ResizableSurfaceNode.(Float) -> Unit = applier { maxWidth = it }
    val SetMaxHeight: ResizableSurfaceNode.(Float) -> Unit = applier { maxHeight = it }
    val SetShape: ResizableSurfaceNode.(Shape) -> Unit = applier { shape = it }
    val SetColor: ResizableSurfaceNode.(Color?) -> Unit = applier { color = it }
    val SetContentColor: ResizableSurfaceNode.(Color?) -> Unit = applier { contentColor = it }
    val SetTonalElevation: ResizableSurfaceNode.(Float) -> Unit = applier { tonalElevation = it }
    val SetShadowElevation: ResizableSurfaceNode.(Float) -> Unit = applier { shadowElevation = it }
  }
}
