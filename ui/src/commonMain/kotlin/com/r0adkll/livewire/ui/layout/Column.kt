package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Column(
  modifier: LivewireModifier = LivewireModifier,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  content: @Composable @LivewireComposable ColumnScope.() -> Unit,
) {
  ReusableComposeNode<ColumnNode, Applier<LayoutNode>>(
    factory = { ColumnNode() },
    update = {
      set(horizontalAlignment, ColumnNode.SetHorizontalAlignment)
      set(modifier, LayoutNode.SetModifier)
    },
    content = { ColumnScopeInstance.content() },
  )
}

@Immutable
interface ColumnScope {
}

@PublishedApi
internal object ColumnScopeInstance : ColumnScope {
}

@Serializable
class ColumnNode(
  var horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) : LayoutNode() {

  companion object {
    val SetHorizontalAlignment: ColumnNode.(Alignment.Horizontal) -> Unit = applier { horizontalAlignment = it }
  }
}
