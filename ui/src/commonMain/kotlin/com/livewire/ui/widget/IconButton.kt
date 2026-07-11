package com.livewire.ui.widget

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonShapes as ComposeIconButtonShapes
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.graphics.Shape
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun IconButton(
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  size: ButtonSize = ButtonSize.Small,
  style: IconButtonStyle = IconButtonStyle.Default,
  shapes: IconButtonShapes = IconButtonShapes(),
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<IconButtonNode, Applier<LayoutNode>>(
    factory = { IconButtonNode(action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(action, IconButtonNode.SetAction)
      set(enabled, IconButtonNode.SetEnabled)
      set(size, IconButtonNode.SetSize)
      set(style, IconButtonNode.SetStyle)
      set(shapes, IconButtonNode.SetShapes)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class IconButtonNode(
  var action: ClickAction,
  var enabled: Boolean = true,
  var size: ButtonSize = ButtonSize.Small,
  var style: IconButtonStyle = IconButtonStyle.Default,
  var shapes: IconButtonShapes = IconButtonShapes(),
) : LayoutNode() {

  companion object {
    val SetAction: IconButtonNode.(ClickAction) -> Unit = applier { action = it }
    val SetEnabled: IconButtonNode.(Boolean) -> Unit = applier { enabled = it }
    val SetSize: IconButtonNode.(ButtonSize) -> Unit = applier { size = it }
    val SetStyle: IconButtonNode.(IconButtonStyle) -> Unit = applier { style = it }
    val SetShapes: IconButtonNode.(IconButtonShapes) -> Unit = applier { shapes = it }
  }
}

@Immutable
@Serializable
data class IconButtonShapes(
  val shape: Shape = CircleShape,
  val pressedShape: Shape = RoundedCornerShape(8.dp),
) {

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  fun toComposeUi(): ComposeIconButtonShapes = ComposeIconButtonShapes(
    shape = shape.toComposeUi(),
    pressedShape = pressedShape.toComposeUi(),
  )
}

enum class IconButtonStyle {
  Default,
  Filled,
  Tonal,
  Outlined,
}
