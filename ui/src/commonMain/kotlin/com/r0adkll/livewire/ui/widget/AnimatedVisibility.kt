package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun AnimatedVisibility(
  visible: Boolean,
  modifier: LivewireModifier = LivewireModifier,
  enter: EnterTransition = EnterTransition.FadeIn,
  exit: ExitTransition = ExitTransition.FadeOut,
  content: @Composable @LivewireComposable () -> Unit,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<AnimatedVisibilityNode, Applier<LayoutNode>>(
    factory = { AnimatedVisibilityNode(visible) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      set(visible, AnimatedVisibilityNode.SetVisible)
      set(enter, AnimatedVisibilityNode.SetEnter)
      set(exit, AnimatedVisibilityNode.SetExit)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
    },
    content = { content() },
  )
}

@LivewireSerializer
@Serializable
class AnimatedVisibilityNode(
  var visible: Boolean,
  var enter: EnterTransition = EnterTransition.FadeIn,
  var exit: ExitTransition = ExitTransition.FadeOut,
) : LayoutNode() {

  companion object {
    val SetVisible: AnimatedVisibilityNode.(Boolean) -> Unit = applier { visible = it }
    val SetEnter: AnimatedVisibilityNode.(EnterTransition) -> Unit = applier { enter = it }
    val SetExit: AnimatedVisibilityNode.(ExitTransition) -> Unit = applier { exit = it }
  }
}

@Serializable
enum class EnterTransition {
  FadeIn,
  SlideInVertically,
  SlideInHorizontally,
  ExpandVertically,
  ExpandHorizontally,
}

@Serializable
enum class ExitTransition {
  FadeOut,
  SlideOutVertically,
  SlideOutHorizontally,
  ShrinkVertically,
  ShrinkHorizontally,
}
