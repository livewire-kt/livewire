package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.SurfaceNode
import kotlinx.coroutines.launch

@Composable
internal fun SurfaceNodeContent(
  node: SurfaceNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val shape = node.shape.toComposeUi()
  val color = node.color ?: MaterialTheme.colorScheme.surface
  val contentColor = node.contentColor ?: contentColorFor(color)
  val tonalElevation = node.tonalElevation.dp
  val shadowElevation = node.shadowElevation.dp

  val content: @Composable () -> Unit = {
    Box {
      node.children.forEach { child ->
        LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
      }
    }
  }

  val clickAction = node.onClick
  if (clickAction != null) {
    Surface(
      onClick = {
        scope.launch {
          eventDispatcher.dispatch(clickAction)
        }
      },
      modifier = modifier.debugFrame(),
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = tonalElevation,
      shadowElevation = shadowElevation,
      content = content,
    )
  } else {
    Surface(
      modifier = modifier.debugFrame(),
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = tonalElevation,
      shadowElevation = shadowElevation,
      content = content,
    )
  }
}
