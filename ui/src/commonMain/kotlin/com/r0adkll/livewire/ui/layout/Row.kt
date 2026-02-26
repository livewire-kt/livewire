package com.r0adkll.livewire.ui.layout

import androidx.annotation.FloatRange
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Stable
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.modifier.DimensionModifier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.WidthModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Row(
  modifier: LivewireModifier = LivewireModifier,
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  ReusableComposeNode<RowNode, Applier<LayoutNode>>(
    factory = { RowNode() },
    update = {
      set(verticalAlignment, RowNode.SetVerticalAlignment)
      set(modifier, LayoutNode.SetModifier)
    },
    content = { RowScopeInstance.content() },
  )
}

@Immutable
interface RowScope {

  @Stable
  fun LivewireModifier.weight(
    @FloatRange(from = 0.0, fromInclusive = false) weight: Float,
  ): LivewireModifier
}

@PublishedApi
internal object RowScopeInstance : RowScope {
  override fun LivewireModifier.weight(weight: Float): LivewireModifier {
    require(weight > 0f) { "weight($weight) must be > 0" }
    return this.then(
      WidthModifier(
        DimensionModifier.Type.WEIGHT,
        weight,
      )
    )
  }
}

@LivewireSerializer
@Serializable
class RowNode(
  var verticalAlignment: Alignment.Vertical = Alignment.Top,
) : LayoutNode() {

  companion object {
    val SetVerticalAlignment: RowNode.(Alignment.Vertical) -> Unit = applier { verticalAlignment = it }
  }
}
