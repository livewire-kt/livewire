package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.AnimatedVisibilityNode
import com.r0adkll.livewire.ui.widget.EnterTransition
import com.r0adkll.livewire.ui.widget.ExitTransition

@Composable
internal fun AnimatedVisibilityNodeContent(
  node: AnimatedVisibilityNode,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = node.visible,
    modifier = modifier.debugFrame(),
    enter = when (node.enter) {
      EnterTransition.FadeIn -> fadeIn()
      EnterTransition.SlideInVertically -> slideInVertically()
      EnterTransition.SlideInHorizontally -> slideInHorizontally()
      EnterTransition.ExpandVertically -> expandVertically()
      EnterTransition.ExpandHorizontally -> expandHorizontally()
    },
    exit = when (node.exit) {
      ExitTransition.FadeOut -> fadeOut()
      ExitTransition.SlideOutVertically -> slideOutVertically()
      ExitTransition.SlideOutHorizontally -> slideOutHorizontally()
      ExitTransition.ShrinkVertically -> shrinkVertically()
      ExitTransition.ShrinkHorizontally -> shrinkHorizontally()
    },
  ) {
    node.children.forEach { child ->
      LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
    }
  }
}
