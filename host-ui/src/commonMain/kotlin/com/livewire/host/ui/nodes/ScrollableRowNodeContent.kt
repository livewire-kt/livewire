package com.livewire.host.ui.nodes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.debugFrame
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.toComposeUi
import com.livewire.ui.widget.ScrollableRowNode

@Composable
internal fun ScrollableRowNodeContent(
  node: ScrollableRowNode,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  Box(modifier.debugFrame()) {
    Row(
      modifier = Modifier.horizontalScroll(
        state = scrollState,
        reverseScrolling = node.reverseScrolling,
      ),
      horizontalArrangement = node.horizontalArrangement.toComposeUi(),
      verticalAlignment = when (node.verticalAlignment) {
        Alignment.Bottom -> androidx.compose.ui.Alignment.Bottom
        Alignment.CenterVertically -> androidx.compose.ui.Alignment.CenterVertically
        Alignment.Top -> androidx.compose.ui.Alignment.Top
      },
    ) {
      node.children.forEach { child ->
        key(child.compositeKeyHash) {
          val modifier = with(child.modifier) { this@Row.toComposeUi(Modifier) }
          LayoutNodeContent(child, modifier)
        }
      }
    }
    if (node.showScrollbar) {
      HorizontalScrollbar(
        scrollState = scrollState,
        modifier = Modifier
          .align(androidx.compose.ui.Alignment.BottomCenter)
          .fillMaxWidth(),
      )
    }
  }
}
