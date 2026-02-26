package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireLayoutNode
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.layout.RowScopeInstance
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun FloatingToolbar(
  expanded: Boolean,
  modifier: LivewireModifier = LivewireModifier,
  content: @Composable @LivewireComposable RowScope.() -> Unit,
) {
  ReusableComposeNode<FloatingToolbarNode, Applier<LayoutNode>>(
    factory = { FloatingToolbarNode(expanded) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(expanded, FloatingToolbarNode.SetExpanded)
    },
    content = { RowScopeInstance.content() },
  )
}

@LivewireLayoutNode
@Serializable
class FloatingToolbarNode(
  var expanded: Boolean,
) : LayoutNode() {

  companion object {
    val SetExpanded: FloatingToolbarNode.(Boolean) -> Unit = applier { expanded = it }
  }
}
