package com.livewire.host.ui.nodes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.host.ui.RemoteIcon
import com.livewire.host.ui.debugFrame
import com.livewire.ui.util.thenIfElse
import com.livewire.ui.widget.TabNode
import com.livewire.ui.widget.TabRowNode
import com.livewire.ui.widget.TabStyle
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
internal fun TabRowNodeContent(
  node: TabRowNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current
  val density = LocalDensity.current
  val scrollState = rememberScrollState()

  // Selection lives on each TabNode; the index is only derived here to place the indicator,
  // so adding/removing tabs can never leave it pointing at a stale position
  val selectedIndex = node.children.indexOfFirst { it is TabNode && it.selected }

  // Offset/width (px) of each tab within the row, measured post-layout to place the indicator
  val tabBounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
  LaunchedEffect(node.children.size) {
    tabBounds.keys.removeAll { it >= node.children.size }
  }

  // Bring the selected tab into view when selection changes
  LaunchedEffect(selectedIndex) {
    tabBounds[selectedIndex]?.let { (offset, width) ->
      val target = (offset + width / 2f - scrollState.viewportSize / 2f)
        .roundToInt()
        .coerceIn(0, scrollState.maxValue)
      scrollState.animateScrollTo(target)
    }
  }

  Box(
    modifier = modifier
      .debugFrame()
      .horizontalScroll(scrollState),
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 8.dp)
        .selectableGroup(),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      node.children.forEachIndexed { index, child ->
        key(child.compositeKeyHash) {
          if (child is TabNode) {
            TabItem(
              node = child,
              style = node.style,
              onClick = {
                scope.launch {
                  eventDispatcher.dispatch(child.onClick)
                }
              },
              modifier = Modifier.onGloballyPositioned { coords ->
                tabBounds[index] = coords.positionInParent().x to coords.size.width.toFloat()
              },
            )
          } else {
            LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
          }
        }
      }
    }

    tabBounds[selectedIndex]?.let { (offset, width) ->
      val indicatorOffset by animateFloatAsState(offset)
      val indicatorWidth by animateFloatAsState(width)

      Box(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .offset { IntOffset(x = indicatorOffset.roundToInt() + 8.dp.roundToPx(), y = 0) }
          .width(with(density) { indicatorWidth.toDp() })
          .height(if (node.style == TabStyle.Primary) 3.dp else 2.dp)
          .background(
            color = MaterialTheme.colorScheme.primary,
            shape = if (node.style == TabStyle.Primary) {
              RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
            } else {
              RoundedCornerShape(0.dp)
            },
          ),
      )
    }
  }
}

@Composable
private fun TabItem(
  node: TabNode,
  style: TabStyle,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val selected = node.selected
  val cornerRadius by animateDpAsState(
    if (selected) 6.dp else 4.dp
  )

  val containerColor by animateColorAsState(
    when {
      style == TabStyle.Secondary -> Color.Transparent
      selected -> MaterialTheme.colorScheme.surfaceContainerHighest
      else -> MaterialTheme.colorScheme.surfaceContainer
    }
  )

  val contentColor by animateColorAsState(
    when {
      !node.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
      selected -> MaterialTheme.colorScheme.primary
      else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
  )

  Row(
    modifier = modifier
      .then(node.modifier.toComposeUi(Modifier))
      .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
      .background(containerColor)
      .selectable(
        selected = selected,
        enabled = node.enabled,
        role = Role.Tab,
        onClick = onClick,
      )
      .defaultMinSize(minHeight = 36.dp)
      .thenIfElse(
        node.children.isEmpty(),
        ifTrue = {
          padding(
            horizontal = 12.dp
          )
        },
        ifFalse = {
          padding(
            start = 12.dp,
            end = 4.dp,
          )
        }
      ),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
      ProvideTextStyle(MaterialTheme.typography.titleSmall) {
        node.icon?.let { icon ->
          RemoteIcon(
            vector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
        }
        node.text?.let { Text(it) }
        node.children.forEach { child ->
          key(child.compositeKeyHash) {
            LayoutNodeContent(child, child.modifier.toComposeUi(Modifier))
          }
        }
      }
    }
  }
}
