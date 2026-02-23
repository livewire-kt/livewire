package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Box(
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable @LivewireComposable BoxScope.() -> Unit,
) {
  ReusableComposeNode<BoxNode, Applier<LayoutNode>>(
    factory = { BoxNode() },
    update = {
      set(contentAlignment, BoxNode.SetContentAlignment)
    },
    content = { BoxScopeInstance.content() },
  )
}

@Immutable
interface BoxScope {
}

@PublishedApi
internal object BoxScopeInstance : BoxScope {

}

@Serializable
class BoxNode(
  var contentAlignment: Alignment = Alignment.TopStart,
) : LayoutNode() {

  companion object {
    val SetContentAlignment: BoxNode.(Alignment) -> Unit = applier { contentAlignment = it }
  }
}
