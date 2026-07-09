package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.RemoteIcon
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.FabSize
import com.livewire.ui.widget.FloatingActionButtonNode
import com.livewire.ui.widget.IconNode
import com.livewire.ui.widget.TextNode
import kotlinx.coroutines.launch

@Composable
internal fun FloatingActionButtonNodeContent(
  node: FloatingActionButtonNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val onClick: () -> Unit = {
    scope.launch {
      eventDispatcher.dispatch(node.action)
    }
  }

  val hasText = node.children.any { it is TextNode }

  // Use ExtendedFloatingActionButton when there is text content
  if (hasText) {
    val iconNode = node.children.filterIsInstance<IconNode>().firstOrNull()
    val textNode = node.children.filterIsInstance<TextNode>().first()

    ExtendedFloatingActionButton(
      onClick = onClick,
      modifier = modifier.debugFrame(),
      expanded = node.expanded,
      icon = {
        if (iconNode != null) {
          RemoteIcon(
            node = iconNode,
            contentDescription = null,
            tint = iconNode.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
            modifier = Modifier.size(24.dp),
          )
        }
      },
      text = { Text(textNode.text) },
    )
  } else {
    // Icon-only FAB — render by size
    val content: @Composable () -> Unit = {
      node.children.forEachIndexed { index, child ->
        when (child) {
          is IconNode -> {
            RemoteIcon(
              node = child,
              contentDescription = null,
              tint = child.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }
    }

    when (node.size) {
      FabSize.Small -> SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier.debugFrame(),
        content = content,
      )
      FabSize.Default -> FloatingActionButton(
        onClick = onClick,
        modifier = modifier.debugFrame(),
        content = content,
      )
      FabSize.Large -> LargeFloatingActionButton(
        onClick = onClick,
        modifier = modifier.debugFrame(),
        content = content,
      )
    }
  }
}
