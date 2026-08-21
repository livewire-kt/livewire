package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Arrangement
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowScope
import com.livewire.ui.layout.RowScopeInstance
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

/**
 * A [com.livewire.ui.layout.Row] whose content scrolls horizontally and that renders a
 * scrollbar on hosts that support one (desktop). On touch-driven hosts the content still
 * scrolls but no scrollbar is drawn.
 */
@LivewireComposable
@Composable
fun ScrollableRow(
  modifier: LivewireModifier = LivewireModifier,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  verticalAlignment: Alignment.Vertical = Alignment.Top,
  showScrollbar: Boolean = true,
  reverseScrolling: Boolean = false,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ScrollableRowNode, Applier<LayoutNode>>(
    factory = { ScrollableRowNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(horizontalArrangement, ScrollableRowNode.SetHorizontalArrangement)
      set(verticalAlignment, ScrollableRowNode.SetVerticalAlignment)
      set(showScrollbar, ScrollableRowNode.SetShowScrollbar)
      set(reverseScrolling, ScrollableRowNode.SetReverseScrolling)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class ScrollableRowNode(
  var horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
  var verticalAlignment: Alignment.Vertical = Alignment.Top,
  var showScrollbar: Boolean = true,
  var reverseScrolling: Boolean = false,
) : LayoutNode() {

  companion object {
    val SetHorizontalArrangement: ScrollableRowNode.(Arrangement.Horizontal) -> Unit = applier { horizontalArrangement = it }
    val SetVerticalAlignment: ScrollableRowNode.(Alignment.Vertical) -> Unit = applier { verticalAlignment = it }
    val SetShowScrollbar: ScrollableRowNode.(Boolean) -> Unit = applier { showScrollbar = it }
    val SetReverseScrolling: ScrollableRowNode.(Boolean) -> Unit = applier { reverseScrolling = it }
  }
}
