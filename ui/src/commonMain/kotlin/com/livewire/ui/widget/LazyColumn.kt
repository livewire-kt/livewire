package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toLong
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.actions.VisibleRangeChangeAction
import com.livewire.ui.actions.visibleRangeChangeAction
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.unit.DpSerializer
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun LazyColumn(
  itemCount: Int,
  modifier: LivewireModifier = LivewireModifier,
  estimatedItemHeight: Dp = 32.dp,
  overscan: Int = 20,
  scrollTo: LazyColumnScroll? = null,
  content: @Composable @LivewireComposable LazyColumnScope.(index: Int) -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()

  var visibleFirst by remember { mutableStateOf(0) }
  var visibleLast by remember { mutableStateOf(InitialWindow - 1) }

  val onVisibleRangeChange = visibleRangeChangeAction(key = compositeKeyHash) { first, last ->
    if (last >= first) {
      visibleFirst = first
      visibleLast = last
    }
  }

  val from = maxOf(0, visibleFirst - overscan)
  val to = minOf(itemCount - 1, visibleLast + overscan)

  ReusableComposeNode<LazyColumnNode, Applier<LayoutNode>>(
    factory = { LazyColumnNode(itemCount, estimatedItemHeight, onVisibleRangeChange) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(itemCount, LazyColumnNode.SetItemCount)
      set(estimatedItemHeight, LazyColumnNode.SetEstimatedItemHeight)
      set(onVisibleRangeChange, LazyColumnNode.SetOnVisibleRangeChange)
      set(scrollTo, LazyColumnNode.SetScrollTo)
    },
    content = {
      for (index in from..to) {
        key(index) {
          LazyColumnItem(index) {
            LazyColumnScopeInstance.content(index)
          }
        }
      }
    },
  )
}

@LivewireComposable
@Composable
private fun LazyColumnItem(
  index: Int,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<LazyColumnItemNode, Applier<LayoutNode>>(
    factory = { LazyColumnItemNode(index) },
    update = {
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(index, LazyColumnItemNode.SetIndex)
    },
    content = { content() },
  )
}

@Immutable
interface LazyColumnScope

internal object LazyColumnScopeInstance : LazyColumnScope

@LivewireSerializer
@Serializable
class LazyColumnNode(
  var itemCount: Int,
  @Serializable(with = DpSerializer::class) var estimatedItemHeight: Dp,
  var onVisibleRangeChange: VisibleRangeChangeAction,
  var scrollTo: LazyColumnScroll? = null,
) : LayoutNode() {

  companion object {
    val SetItemCount: LazyColumnNode.(Int) -> Unit = applier { itemCount = it }
    val SetEstimatedItemHeight: LazyColumnNode.(Dp) -> Unit = applier { estimatedItemHeight = it }
    val SetOnVisibleRangeChange: LazyColumnNode.(VisibleRangeChangeAction) -> Unit = applier { onVisibleRangeChange = it }
    val SetScrollTo: LazyColumnNode.(LazyColumnScroll?) -> Unit = applier { scrollTo = it }
  }
}

@LivewireSerializer
@Serializable
class LazyColumnItemNode(
  var index: Int,
) : LayoutNode() {

  companion object {
    val SetIndex: LazyColumnItemNode.(Int) -> Unit = applier { index = it }
  }
}

private const val InitialWindow = 20

@Serializable
data class LazyColumnScroll(
  val index: Int,
  val token: Long,
)
