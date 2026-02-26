package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.ProgressIndicatorNode
import com.r0adkll.livewire.ui.widget.ProgressIndicatorStyle

@Composable
internal fun ProgressIndicatorNodeContent(
  node: ProgressIndicatorNode,
  modifier: Modifier = Modifier,
) {
  val progress = node.progress

  when (node.style) {
    ProgressIndicatorStyle.Linear -> {
      if (progress != null) {
        LinearProgressIndicator(
          progress = { progress },
          modifier = modifier.debugFrame(),
        )
      } else {
        LinearProgressIndicator(
          modifier = modifier.debugFrame(),
        )
      }
    }
    ProgressIndicatorStyle.Circular -> {
      if (progress != null) {
        CircularProgressIndicator(
          progress = { progress },
          modifier = modifier.debugFrame(),
        )
      } else {
        CircularProgressIndicator(
          modifier = modifier.debugFrame(),
        )
      }
    }
  }
}
