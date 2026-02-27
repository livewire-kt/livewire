package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Spacer(
  modifier: LivewireModifier = LivewireModifier,
) {
  ReusableComposeNode<SpacerNode, Applier<LayoutNode>>(
    factory = { SpacerNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
    },
  )
}

@LivewireSerializer
@Serializable
class SpacerNode : LayoutNode()
