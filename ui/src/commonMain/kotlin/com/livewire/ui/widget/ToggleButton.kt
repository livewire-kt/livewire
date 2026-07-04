package com.livewire.ui.widget

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ToggleButtonShapes as ComposeToggleButtonShapes
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.CheckedChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.graphics.Shape
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowScope
import com.livewire.ui.layout.RowScopeInstance
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun ToggleButton(
  checked: Boolean,
  onCheckedChange: CheckedChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  size: ButtonSize = ButtonSize.Small,
  style: ToggleButtonStyle = ToggleButtonStyle.Filled,
  shapes: ToggleButtonShapes = ToggleButtonShapes(),
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ToggleButtonNode, Applier<LayoutNode>>(
    factory = { ToggleButtonNode(checked, onCheckedChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(checked, ToggleButtonNode.SetChecked)
      set(onCheckedChange, ToggleButtonNode.SetOnCheckedChange)
      set(enabled, ToggleButtonNode.SetEnabled)
      set(size, ToggleButtonNode.SetSize)
      set(style, ToggleButtonNode.SetStyle)
      set(shapes, ToggleButtonNode.SetShapes)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class ToggleButtonNode(
  var checked: Boolean,
  var onCheckedChange: CheckedChangeAction,
  var enabled: Boolean = true,
  var size: ButtonSize = ButtonSize.Small,
  var style: ToggleButtonStyle = ToggleButtonStyle.Filled,
  var shapes: ToggleButtonShapes = ToggleButtonShapes(),
) : LayoutNode() {

  companion object {
    val SetChecked: ToggleButtonNode.(Boolean) -> Unit = applier { checked = it }
    val SetOnCheckedChange: ToggleButtonNode.(CheckedChangeAction) -> Unit = applier { onCheckedChange = it }
    val SetEnabled: ToggleButtonNode.(Boolean) -> Unit = applier { enabled = it }
    val SetSize: ToggleButtonNode.(ButtonSize) -> Unit = applier { size = it }
    val SetStyle: ToggleButtonNode.(ToggleButtonStyle) -> Unit = applier { style = it }
    val SetShapes: ToggleButtonNode.(ToggleButtonShapes) -> Unit = applier { shapes = it }
  }
}

@Serializable
data class ToggleButtonShapes(
  val shape: Shape = CircleShape,
  val pressedShape: Shape = RoundedCornerShape(6.dp),
  val checkedShape: Shape = RoundedCornerShape(12.dp),
) {

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  fun toComposeUi(): ComposeToggleButtonShapes = ComposeToggleButtonShapes(
    shape = shape.toComposeUi(),
    pressedShape = pressedShape.toComposeUi(),
    checkedShape = checkedShape.toComposeUi(),
  )
}

@Serializable
enum class ToggleButtonStyle {
  Filled,
  Outlined,
  Elevated,
}
