package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun ProgressIndicator(
  modifier: LivewireModifier = LivewireModifier,
  progress: Float? = null,
  style: ProgressIndicatorStyle = ProgressIndicatorStyle.Linear,
) {
  ReusableComposeNode<ProgressIndicatorNode, Applier<LayoutNode>>(
    factory = { ProgressIndicatorNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(progress, ProgressIndicatorNode.SetProgress)
      set(style, ProgressIndicatorNode.SetStyle)
    },
  )
}

@LivewireLayoutNode
@Serializable
class ProgressIndicatorNode(
  var progress: Float? = null,
  var style: ProgressIndicatorStyle = ProgressIndicatorStyle.Linear,
) : LayoutNode() {

  companion object {
    val SetProgress: ProgressIndicatorNode.(Float?) -> Unit = applier { progress = it }
    val SetStyle: ProgressIndicatorNode.(ProgressIndicatorStyle) -> Unit = applier { style = it }
  }
}

@Serializable
enum class ProgressIndicatorStyle {
  Linear,
  Circular,
}
