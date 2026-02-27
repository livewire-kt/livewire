package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
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
      val isSelected = index == node.selectedTabIndex

      val tabCornerRadius by animateDpAsState(
        if (isSelected) 16.dp else 8.dp
      )

      val tabContainerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainer
      )

      if (child is TabNode) {
        Tab(
          selected = isSelected,
          onClick = {
            scope.launch {
              eventDispatcher.dispatch(node.onTabSelected.copy(value = index))
            }
          },
          modifier = child.modifier.toComposeUi(Modifier)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(topStart = tabCornerRadius, topEnd = tabCornerRadius))
            .background(tabContainerColor),
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
      edgePadding = 8.dp,
      tabs = content,
      indicator = {
        TabRowDefaults.PrimaryIndicator(
          Modifier.tabIndicatorOffset(node.selectedTabIndex, matchContentSize = false),
          width = Dp.Unspecified,
        )
      }
    )

    TabStyle.Secondary -> SecondaryScrollableTabRow(
      selectedTabIndex = node.selectedTabIndex,
      modifier = modifier.debugFrame(),
      edgePadding = 8.dp,
      tabs = content,
      indicator = {
        TabRowDefaults.PrimaryIndicator(
          Modifier.tabIndicatorOffset(node.selectedTabIndex, matchContentSize = false),
          width = Dp.Unspecified,
        )
      }
    )
  }
}
