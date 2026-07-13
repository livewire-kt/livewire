package com.livewire.host.ui.nodes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.host.ui.debugFrame
import com.livewire.ui.widget.BasicTextFieldNode
import kotlinx.coroutines.launch

@Composable
internal fun BasicTextFieldNodeContent(
  node: BasicTextFieldNode,
  modifier: Modifier = Modifier,
) = key(node.compositeKeyHash) {
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val textFieldState = rememberTextFieldState(node.initialValue)
  LaunchedEffect(textFieldState.text) {
    eventDispatcher.dispatch(node.onValueChange.copy(
      value = textFieldState.text.toString()
    ))
  }

  val textColor = if (node.textColor.isSpecified) node.textColor else LocalContentColor.current
  val textStyle = node.textStyle.asComposeTextStyle.merge(
    color = textColor,
    fontWeight = node.fontWeight?.let { FontWeight(it) },
  )
  val cursorColor = if (node.cursorColor.isSpecified) {
    node.cursorColor
  } else {
    MaterialTheme.colorScheme.primary
  }

  val scope = rememberCoroutineScope()
  val onSubmit = node.onSubmit
  val onCancel = node.onCancel
  val keyEventModifier = if (onSubmit != null || onCancel != null) {
    Modifier.onPreviewKeyEvent { event ->
      if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
      when {
        onSubmit != null &&
          (event.key == Key.Enter || event.key == Key.NumPadEnter) &&
          // in multi-line fields plain Enter inserts a newline; submit is Cmd/Ctrl+Enter
          (node.singleLine || event.isMetaPressed || event.isCtrlPressed) -> {
          scope.launch { eventDispatcher.dispatch(onSubmit) }
          true
        }

        onCancel != null && event.key == Key.Escape -> {
          scope.launch { eventDispatcher.dispatch(onCancel) }
          true
        }

        else -> false
      }
    }
  } else {
    Modifier
  }

  BasicTextField(
    state = textFieldState,
    modifier = modifier.debugFrame().then(keyEventModifier),
    enabled = node.enabled,
    readOnly = node.readOnly,
    textStyle = textStyle,
    lineLimits = if (node.singleLine) {
      TextFieldLineLimits.SingleLine
    } else {
      TextFieldLineLimits.MultiLine(
        minHeightInLines = node.minLines,
        maxHeightInLines = node.maxLines,
      )
    },
    cursorBrush = SolidColor(cursorColor),
    decorator = node.placeholder?.let { placeholder ->
      TextFieldDecorator { innerTextField ->
        Box {
          if (textFieldState.text.isEmpty()) {
            Text(
              text = placeholder,
              style = textStyle,
              color = textColor.copy(alpha = 0.5f),
            )
          }
          innerTextField()
        }
      }
    },
  )
}
