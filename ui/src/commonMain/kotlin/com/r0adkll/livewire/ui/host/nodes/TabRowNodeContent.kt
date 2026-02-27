package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.layout.size
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.host.RemoteIcon
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.TabNode
import com.r0adkll.livewire.ui.widget.TabRowNode
import com.r0adkll.livewire.ui.widget.TabStyle
import kotlinx.coroutines.launch

@Composable
internal fun TabRowNodeContent(
  node: TabRowNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val content: @Composable () -> Unit = {
    node.children.forEachIndexed { index, child ->
      if (child is TabNode) {
        Tab(
          selected = index == node.selectedTabIndex,
          onClick = {
            scope.launch {
              eventDispatcher.dispatch(node.onTabSelected.copy(value = index))
            }
          },
          modifier = child.modifier.toComposeUi(Modifier),
          enabled = child.enabled,
          text = child.text?.let { { Text(it) } },
          icon = child.iconData?.let { iconData ->
            {
              RemoteIcon(
                svgData = iconData,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
              )
            }
          },
        )
      } else {
        LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
      }
    }
  }

  when (node.style) {
    TabStyle.Primary -> PrimaryScrollableTabRow(
      selectedTabIndex = node.selectedTabIndex,
      modifier = modifier.debugFrame(),
      tabs = content,
    )

    TabStyle.Secondary -> SecondaryScrollableTabRow(
      selectedTabIndex = node.selectedTabIndex,
      modifier = modifier.debugFrame(),
      tabs = content,
    )
  }
}
