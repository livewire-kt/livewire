package com.r0adkll.livewire.ui.layout

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
inline fun Box(
  modifier: LivewireModifier = LivewireModifier,
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable @LivewireComposable BoxScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<BoxNode, Applier<LayoutNode>>(
    factory = { BoxNode() },
    update = {
      set(contentAlignment, BoxNode.SetContentAlignment)
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
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

@LivewireSerializer
@Serializable
class BoxNode(
  var contentAlignment: Alignment = Alignment.TopStart,
) : LayoutNode() {

  companion object {
    val SetContentAlignment: BoxNode.(Alignment) -> Unit = applier { contentAlignment = it }
  }
}
