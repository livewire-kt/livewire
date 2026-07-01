package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.ColorSerializer
import com.r0adkll.livewire.ui.graphics.RectangleShape
import com.r0adkll.livewire.ui.graphics.Shape
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable

/**
 * A [Surface] that can be resized by dragging a handle in the bottom-end corner.
 */
@LivewireComposable
@Composable
fun ResizableSurface(
  anchor: ResizeAnchor,
  initialSize: Dp,
  modifier: LivewireModifier = LivewireModifier,
  minSize: Dp = initialSize,
  maxSize: Dp = Dp.Infinity,
  shape: Shape = RectangleShape,
  color: Color? = null,
  contentColor: Color? = null,
  tonalElevation: Dp = 0.dp,
  shadowElevation: Dp = 0.dp,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ResizableSurfaceNode, Applier<LayoutNode>>(
    factory = { ResizableSurfaceNode(anchor, initialSize, minSize, maxSize) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(anchor, ResizableSurfaceNode.SetAnchor)
      update(initialSize, ResizableSurfaceNode.SetInitialSize)
      update(minSize, ResizableSurfaceNode.SetMinSize)
      update(maxSize, ResizableSurfaceNode.SetMaxSize)
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
  var anchor: ResizeAnchor,
  @Serializable(with = DpSerializer::class) var initialSize: Dp,
  @Serializable(with = DpSerializer::class) var minSize: Dp,
  @Serializable(with = DpSerializer::class) var maxSize: Dp,
  var shape: Shape = RectangleShape,
  @Serializable(with = ColorSerializer::class) var color: Color? = null,
  @Serializable(with = ColorSerializer::class) var contentColor: Color? = null,
  @Serializable(with = DpSerializer::class) var tonalElevation: Dp = 0.dp,
  @Serializable(with = DpSerializer::class) var shadowElevation: Dp = 0.dp,
) : LayoutNode() {

  companion object {
    val SetAnchor: ResizableSurfaceNode.(ResizeAnchor) -> Unit = applier { anchor = it }
    val SetInitialSize: ResizableSurfaceNode.(Dp) -> Unit = applier { initialSize = it }
    val SetMinSize: ResizableSurfaceNode.(Dp) -> Unit = applier { minSize = it }
    val SetMaxSize: ResizableSurfaceNode.(Dp) -> Unit = applier { maxSize = it }
    val SetShape: ResizableSurfaceNode.(Shape) -> Unit = applier { shape = it }
    val SetColor: ResizableSurfaceNode.(Color?) -> Unit = applier { color = it }
    val SetContentColor: ResizableSurfaceNode.(Color?) -> Unit = applier { contentColor = it }
    val SetTonalElevation: ResizableSurfaceNode.(Dp) -> Unit = applier { tonalElevation = it }
    val SetShadowElevation: ResizableSurfaceNode.(Dp) -> Unit = applier { shadowElevation = it }
  }
}

@Serializable
enum class ResizeAnchor {
  Start, Top, End, Bottom;

  val isHorizontal: Boolean get() = this == Start || this == End
  val isVertical: Boolean get() = this == Top || this == Bottom
}
