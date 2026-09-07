package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.debugFrame
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.widget.LazyColumnItemNode
import com.livewire.ui.widget.LazyColumnNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate

@Composable
internal fun LazyColumnNodeContent(
  node: LazyColumnNode,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val dispatcher = LocalLivewireActionDispatcher.current
  val action = node.onVisibleRangeChange

  LaunchedEffect(listState, action.identifier) {
    snapshotFlow {
      val visible = listState.layoutInfo.visibleItemsInfo
      if (visible.isEmpty()) null else visible.first().index to visible.last().index
    }
      .conflate()
      .collect { range ->
        if (range != null) {
          dispatcher.dispatch(action.copy(first = range.first, last = range.second))
          delay(RangeReportIntervalMs)
        }
      }
  }

  val scrollTo = node.scrollTo
  LaunchedEffect(scrollTo) {
    if (scrollTo != null && scrollTo.index in 0 until node.itemCount) {
      listState.animateScrollToItem(scrollTo.index)
    }
  }

  val itemsByIndex = node.children
    .filterIsInstance<LazyColumnItemNode>()
    .associateBy { it.index }

  LazyColumn(
    modifier = modifier.debugFrame(),
    state = listState,
  ) {
    items(count = node.itemCount, key = { it }) { index ->
      val item = itemsByIndex[index]
      if (item == null) {
        Spacer(Modifier.fillMaxWidth().height(node.estimatedItemHeight))
      } else {
        LayoutNodeContent(item, item.modifier.toComposeUi(Modifier))
      }
    }
  }
}

@Composable
internal fun LazyColumnItemNodeContent(
  node: LazyColumnItemNode,
  modifier: Modifier = Modifier,
) {
  Column(modifier.debugFrame()) {
    node.children.forEach { child ->
      key(child.compositeKeyHash) {
        val childModifier = with(child.modifier) { this@Column.toComposeUi(Modifier) }
        LayoutNodeContent(child, childModifier)
      }
    }
  }
}

private const val RangeReportIntervalMs = 50L
