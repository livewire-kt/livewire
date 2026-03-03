package com.r0adkll.livewire.ui.layout

import androidx.annotation.FloatRange
import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.modifier.AlignModifier
import com.r0adkll.livewire.ui.modifier.DimensionModifier
import com.r0adkll.livewire.ui.modifier.HeightModifier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Column(
  modifier: LivewireModifier = LivewireModifier,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable @LivewireComposable ColumnScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<ColumnNode, Applier<LayoutNode>>(
    factory = { ColumnNode() },
    update = {
      set(verticalArrangement, ColumnNode.SetVerticalArrangement)
      set(horizontalAlignment, ColumnNode.SetHorizontalAlignment)
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
    },
    content = { ColumnScopeInstance.content() },
  )
}

@Immutable
interface ColumnScope {

  @Stable
  fun LivewireModifier.align(alignment: Alignment.Horizontal): LivewireModifier

  @Stable
  fun LivewireModifier.weight(
    @FloatRange(from = 0.0, fromInclusive = false) weight: Float,
  ): LivewireModifier
}

@PublishedApi
internal object ColumnScopeInstance : ColumnScope {
  override fun LivewireModifier.align(alignment: Alignment.Horizontal): LivewireModifier {
    return then(AlignModifier(alignment))
  }

  override fun LivewireModifier.weight(
    weight: Float,
  ): LivewireModifier {
    require(weight > 0f) { "weight($weight) must be > 0" }
    return this.then(
      HeightModifier(
        DimensionModifier.Type.WEIGHT,
        weight,
      )
    )
  }
}

@LivewireSerializer
@Serializable
class ColumnNode(
  var verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  var horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) : LayoutNode() {

  companion object {
    val SetVerticalArrangement: ColumnNode.(Arrangement.Vertical) -> Unit = applier { verticalArrangement = it }
    val SetHorizontalAlignment: ColumnNode.(Alignment.Horizontal) -> Unit = applier { horizontalAlignment = it }
  }
}
