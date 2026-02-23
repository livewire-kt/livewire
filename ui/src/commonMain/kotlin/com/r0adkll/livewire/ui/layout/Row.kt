package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Row(
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  ReusableComposeNode<RowNode, Applier<LayoutNode>>(
    factory = { RowNode() },
    update = {
      set(verticalAlignment, RowNode.SetVerticalAlignment)
    },
    content = { RowScopeInstance.content() },
  )
}

@Immutable
interface RowScope {
}

@PublishedApi
internal object RowScopeInstance : RowScope {
}

@Serializable
class RowNode(
  var verticalAlignment: Alignment.Vertical = Alignment.Top,
) : LayoutNode() {

  companion object {
    val SetVerticalAlignment: RowNode.(Alignment.Vertical) -> Unit = applier { verticalAlignment = it }
  }
}
