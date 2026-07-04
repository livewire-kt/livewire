package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun HorizontalDivider(
  modifier: LivewireModifier = LivewireModifier,
  thickness: Dp = 1.dp,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<DividerNode, Applier<LayoutNode>>(
    factory = { DividerNode(DividerStyle.Horizontal, thickness) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(thickness, DividerNode.SetThickness)
    },
  )
}

@LivewireComposable
@Composable
fun VerticalDivider(
  modifier: LivewireModifier = LivewireModifier,
  thickness: Dp = 1.dp,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<DividerNode, Applier<LayoutNode>>(
    factory = { DividerNode(DividerStyle.Vertical, thickness) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(thickness, DividerNode.SetThickness)
    },
  )
}

@LivewireSerializer
@Serializable
class DividerNode(
  var style: DividerStyle = DividerStyle.Horizontal,
  @Serializable(with = DpSerializer::class) var thickness: Dp = 1.dp,
) : LayoutNode() {

  companion object {
    val SetStyle: DividerNode.(DividerStyle) -> Unit = applier { style = it }
    val SetThickness: DividerNode.(Dp) -> Unit = applier { thickness = it }
  }
}

@Serializable
enum class DividerStyle {
  Horizontal,
  Vertical,
}
