package com.livewire.ui.host.nodes

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.host.RemoteIcon
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.IconNode
import com.livewire.ui.widget.TextNode
import com.livewire.ui.widget.ToggleButtonNode
import com.livewire.ui.widget.ToggleButtonStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ToggleButtonNodeContent(
  node: ToggleButtonNode,
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

  ToggleButtonNodeContent(
    checked = node.checked,
    onCheckedChange = { checked ->
      scope.launch {
        eventDispatcher.dispatch(node.onCheckedChange.copy(checked = checked))
      }
    },
    style = node.style,
    enabled = node.enabled,
    shapes = node.shapes.toComposeUi(),
    modifier = modifier
      .heightIn(buttonSize)
      .debugFrame(),
  ) {
    node.children.forEachIndexed { index, child ->
      when (child) {
        is TextNode -> {
          Text(
            text = child.text,
            style = ButtonDefaults.textStyleFor(buttonSize),
          )
        }

        is IconNode -> {
          RemoteIcon(
            node = child,
            contentDescription = null,
            tint = child.tint,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonSize))
          )
        }
      }

      if (index < node.children.lastIndex && node.children.size > 1) {
        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(buttonSize)))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ToggleButtonNodeContent(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  style: ToggleButtonStyle,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  shapes: ToggleButtonShapes = ToggleButtonDefaults.shapes(),
  content: @Composable RowScope.() -> Unit,
) {
  when (style) {
    ToggleButtonStyle.Filled -> ToggleButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
    ToggleButtonStyle.Outlined -> OutlinedToggleButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
    ToggleButtonStyle.Elevated -> ElevatedToggleButton(
      checked = checked,
      onCheckedChange = onCheckedChange,
      enabled = enabled,
      shapes = shapes,
      modifier = modifier,
      content = content,
    )
  }
}
