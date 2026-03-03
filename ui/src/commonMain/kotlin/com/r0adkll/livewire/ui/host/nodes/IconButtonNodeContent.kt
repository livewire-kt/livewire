package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.host.RemoteIcon
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.IconButtonNode
import com.r0adkll.livewire.ui.widget.IconButtonStyle
import com.r0adkll.livewire.ui.widget.IconNode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun IconButtonNodeContent(
  node: IconButtonNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val buttonSize = when (node.size) {
    ButtonSize.ExtraSmall -> ButtonDefaults.ExtraSmallContainerHeight
    ButtonSize.Small -> ButtonDefaults.MinHeight
    ButtonSize.Medium -> ButtonDefaults.MediumContainerHeight
    ButtonSize.Large -> ButtonDefaults.LargeContainerHeight
  }

  IconButtonNodeContent(
    onClick = {
      scope.launch {
        eventDispatcher.dispatch(node.action)
      }
    },
    style = node.style,
    shapes = IconButtonDefaults.shapes(),
    enabled = node.enabled,
    modifier = modifier
      .size(buttonSize)
      .debugFrame(),
  ) {
    val child = node.children.firstOrNull() ?: return@IconButtonNodeContent
    require(child is IconNode) { "Only IconNode can be used in an IconButton content." }

    RemoteIcon(
      svgData = child.svgData,
      contentDescription = null,
      tint = child.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
      modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonSize))
    )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IconButtonNodeContent(
  style: IconButtonStyle,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shapes: IconButtonShapes = IconButtonDefaults.shapes(),
  content: @Composable () -> Unit,
) {
  when (style) {
    IconButtonStyle.Default -> IconButton(
      enabled = enabled,
      onClick = onClick,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
    IconButtonStyle.Filled -> FilledIconButton(
      enabled = enabled,
      onClick = onClick,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
    IconButtonStyle.Tonal -> FilledTonalIconButton(
      enabled = enabled,
      onClick = onClick,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
    IconButtonStyle.Outlined -> OutlinedIconButton(
      enabled = enabled,
      onClick = onClick,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
  }
}
