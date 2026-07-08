package com.livewire.ui.host.nodes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.host.RemoteIcon
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.DropdownMenuItemNode
import com.livewire.ui.widget.DropdownMenuNode
import kotlinx.coroutines.launch

@Composable
internal fun DropdownMenuNodeContent(
  node: DropdownMenuNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  DropdownMenu(
    expanded = node.expanded,
    onDismissRequest = {
      scope.launch {
        eventDispatcher.dispatch(node.onDismissRequest)
      }
    },
    modifier = modifier.debugFrame(),
  ) {
    node.children.forEach { child ->
      if (child is DropdownMenuItemNode) {
        key(child.compositeKeyHash) {
          DropdownMenuItem(
            text = { Text(child.text) },
            onClick = {
              scope.launch {
                eventDispatcher.dispatch(child.onClick)
              }
            },
            modifier = child.modifier.toComposeUi(Modifier),
            enabled = child.enabled,
            leadingIcon = child.leadingIcon?.let { icon ->
              {
                RemoteIcon(
                  vector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                )
              }
            },
            trailingIcon = child.trailingIcon?.let { icon ->
              {
                RemoteIcon(
                  vector = icon,
                  contentDescription = null,
                )
              }
            },
          )
        }
      }
    }
  }
}
