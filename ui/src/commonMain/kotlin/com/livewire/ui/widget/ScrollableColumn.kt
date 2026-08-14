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
import com.livewire.ui.layout.ColumnScope
import com.livewire.ui.layout.ColumnScopeInstance
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

/**
 * A [com.livewire.ui.layout.Column] whose content scrolls vertically and that renders a
 * scrollbar on hosts that support one (desktop). On touch-driven hosts the content still
 * scrolls but no scrollbar is drawn.
 */
@LivewireComposable
@Composable
fun ScrollableColumn(
  modifier: LivewireModifier = LivewireModifier,
  verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  showScrollbar: Boolean = true,
  reverseScrolling: Boolean = false,
  content: @Composable @LivewireComposable ColumnScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ScrollableColumnNode, Applier<LayoutNode>>(
    factory = { ScrollableColumnNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(verticalArrangement, ScrollableColumnNode.SetVerticalArrangement)
      set(horizontalAlignment, ScrollableColumnNode.SetHorizontalAlignment)
      set(showScrollbar, ScrollableColumnNode.SetShowScrollbar)
      set(reverseScrolling, ScrollableColumnNode.SetReverseScrolling)
    },
    content = { ColumnScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class ScrollableColumnNode(
  var verticalArrangement: Arrangement.Vertical = Arrangement.Top,
  var horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  var showScrollbar: Boolean = true,
  var reverseScrolling: Boolean = false,
) : LayoutNode() {

  companion object {
    val SetVerticalArrangement: ScrollableColumnNode.(Arrangement.Vertical) -> Unit = applier { verticalArrangement = it }
    val SetHorizontalAlignment: ScrollableColumnNode.(Alignment.Horizontal) -> Unit = applier { horizontalAlignment = it }
    val SetShowScrollbar: ScrollableColumnNode.(Boolean) -> Unit = applier { showScrollbar = it }
    val SetReverseScrolling: ScrollableColumnNode.(Boolean) -> Unit = applier { reverseScrolling = it }
  }
}
