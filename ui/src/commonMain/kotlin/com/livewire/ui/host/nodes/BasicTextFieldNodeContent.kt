package com.livewire.ui.host.nodes

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.font.FontWeight
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.host.debugFrame
import com.livewire.ui.widget.BasicTextFieldNode

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

  BasicTextField(
    state = textFieldState,
    modifier = modifier.debugFrame(),
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
