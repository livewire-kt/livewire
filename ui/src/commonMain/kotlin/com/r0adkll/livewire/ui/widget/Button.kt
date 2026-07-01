package com.r0adkll.livewire.ui.widget

import androidx.compose.material3.ButtonShapes as ComposeButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.graphics.Shape
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.layout.RowScopeInstance
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable
import androidx.compose.runtime.toLong

@LivewireComposable
@Composable
fun Button(
  action: ClickAction,
  modifier: LivewireModifier = LivewireModifier,
  size: ButtonSize = ButtonSize.Small,
  style: ButtonStyle = ButtonStyle.Filled,
  shapes: ButtonShapes = ButtonShapes(),
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ButtonNode, Applier<LayoutNode>>(
    factory = { ButtonNode(action) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      update(action, ButtonNode.SetAction)
      set(size, ButtonNode.SetSize)
      set(style, ButtonNode.SetStyle)
      set(shapes, ButtonNode.SetShapes)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class ButtonNode(
  var action: ClickAction,
  var size: ButtonSize = ButtonSize.Small,
  var style: ButtonStyle = ButtonStyle.Filled,
  var shapes: ButtonShapes = ButtonShapes(),
) : LayoutNode() {

  companion object {
    val SetAction: ButtonNode.(ClickAction) -> Unit = applier { action = it }
    val SetSize: ButtonNode.(ButtonSize) -> Unit = applier { size = it }
    val SetStyle: ButtonNode.(ButtonStyle) -> Unit = applier { style = it }
    val SetShapes: ButtonNode.(ButtonShapes) -> Unit = applier { shapes = it }
  }
}

@Immutable
@Serializable
data class ButtonShapes(
  val shape: Shape = CircleShape,
  val pressedShape: Shape = RoundedCornerShape(8.dp),
) {

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  fun toComposeUi(): ComposeButtonShapes = ComposeButtonShapes(
    shape = shape.toComposeUi(),
    pressedShape = pressedShape.toComposeUi(),
  )
}

enum class ButtonSize {
  ExtraSmall,
  Small,
  Medium,
  Large,
}

enum class ButtonStyle {
  Filled,
  Tonal,
  Outlined,
  Elevated,
  Text,
}
