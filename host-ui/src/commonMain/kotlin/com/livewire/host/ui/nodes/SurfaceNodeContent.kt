package com.livewire.host.ui.nodes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.SurfaceNode
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
  val tonalElevation = node.tonalElevation
  val shadowElevation = node.shadowElevation

  val content: @Composable () -> Unit = {
    node.children.forEach { child ->
      key(child.compositeKeyHash) {
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
