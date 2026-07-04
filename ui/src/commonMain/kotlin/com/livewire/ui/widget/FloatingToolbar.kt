package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.RowScope
import com.livewire.ui.layout.RowScopeInstance
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun FloatingToolbar(
  expanded: Boolean,
  modifier: LivewireModifier = LivewireModifier,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<FloatingToolbarNode, Applier<LayoutNode>>(
    factory = { FloatingToolbarNode(expanded) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(expanded, FloatingToolbarNode.SetExpanded)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireSerializer
@Serializable
class FloatingToolbarNode(
  var expanded: Boolean,
) : LayoutNode() {

  companion object {
    val SetExpanded: FloatingToolbarNode.(Boolean) -> Unit = applier { expanded = it }
  }
}
