package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.RemoteIcon
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.ButtonNode
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.IconNode
import com.livewire.ui.widget.TextNode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ButtonNodeContent(
  node: ButtonNode,
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

  ButtonNodeContent(
    onClick = {
      scope.launch {
        eventDispatcher.dispatch(node.action)
      }
    },
    style = node.style,
    shapes = node.shapes.toComposeUi(),
    contentPadding = ButtonDefaults.contentPaddingFor(buttonSize),
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
            tint = child.tint.takeIf { it != Color.Unspecified } ?: LocalContentColor.current,
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
private fun ButtonNodeContent(
  style: ButtonStyle,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shapes: ButtonShapes = ButtonDefaults.shapes(),
  contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
  content: @Composable RowScope.() -> Unit,
) {
  when (style) {
    ButtonStyle.Filled -> Button(
      onClick = onClick,
      shapes = shapes,
      contentPadding = contentPadding,
      modifier = modifier,
      content = content,
    )
    ButtonStyle.Tonal -> FilledTonalButton(
      onClick = onClick,
      shapes = shapes,
      contentPadding = contentPadding,
      modifier = modifier,
      content = content,
    )
    ButtonStyle.Outlined -> OutlinedButton(
      onClick = onClick,
      shapes = shapes,
      contentPadding = contentPadding,
      modifier = modifier,
      content = content,
    )
    ButtonStyle.Elevated -> ElevatedButton(
      onClick = onClick,
      shapes = shapes,
      contentPadding = contentPadding,
      modifier = modifier,
      content = content,
    )
    ButtonStyle.Text -> TextButton(
      onClick = onClick,
      shapes = shapes,
      contentPadding = contentPadding,
      modifier = modifier,
      content = content,
    )
  }
}
