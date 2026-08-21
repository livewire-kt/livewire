package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.debugFrame
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.toComposeUi
import com.livewire.ui.widget.ScrollableColumnNode

@Composable
internal fun ScrollableColumnNodeContent(
  node: ScrollableColumnNode,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  Box(modifier.debugFrame()) {
    Column(
      modifier = Modifier.verticalScroll(
        state = scrollState,
        reverseScrolling = node.reverseScrolling,
      ),
      verticalArrangement = node.verticalArrangement.toComposeUi(),
      horizontalAlignment = when (node.horizontalAlignment) {
        Alignment.CenterHorizontally -> androidx.compose.ui.Alignment.CenterHorizontally
        Alignment.End -> androidx.compose.ui.Alignment.End
        Alignment.Start -> androidx.compose.ui.Alignment.Start
      },
    ) {
      node.children.forEach { child ->
        // Keyed by the guest composition's identity so host-side state
        // survives siblings being inserted or removed above this child.
        key(child.compositeKeyHash) {
          val modifier = with(child.modifier) { this@Column.toComposeUi(Modifier) }
          LayoutNodeContent(child, modifier)
        }
      }
    }
    if (node.showScrollbar) {
      VerticalScrollbar(
        scrollState = scrollState,
        modifier = Modifier
          .align(androidx.compose.ui.Alignment.CenterEnd)
          .fillMaxHeight(),
      )
    }
  }
}
