package com.r0adkll.livewire.ui.host.nodes

import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.actions.ValueChangeAction
import com.r0adkll.livewire.ui.host.debugFrame
import com.r0adkll.livewire.ui.widget.TextFieldNode
import com.r0adkll.livewire.ui.widget.TextFieldStyle
import kotlinx.coroutines.launch

@Composable
internal fun TextFieldNodeContent(
  node: TextFieldNode,
  modifier: Modifier = Modifier,
) = key(node.compositeKeyHash) {
  val eventDispatcher = LocalLivewireActionDispatcher.current

  val textFieldState = rememberTextFieldState(node.initialValue)
  LaunchedEffect(textFieldState.text) {
    eventDispatcher.dispatch(node.onValueChange.copy(
      value = textFieldState.text.toString()
    ))
  }

  val label: (@Composable TextFieldLabelScope.() -> Unit)? = node.label?.let { { Text(it) } }
  val placeholder: (@Composable () -> Unit)? = node.placeholder?.let { { Text(it) } }

  when (node.style) {
    TextFieldStyle.Filled -> TextField(
      state = textFieldState,
      modifier = modifier.debugFrame(),
      enabled = node.enabled,
      readOnly = node.readOnly,
      label = label,
      placeholder = placeholder,
      lineLimits = if (node.singleLine) {
        TextFieldLineLimits.SingleLine
      } else {
        TextFieldLineLimits.MultiLine(maxHeightInLines = node.maxLines)
      }
    )
    TextFieldStyle.Outlined -> OutlinedTextField(
      state = textFieldState,
      modifier = modifier.debugFrame(),
      enabled = node.enabled,
      readOnly = node.readOnly,
      label = label,
      placeholder = placeholder,
      lineLimits = if (node.singleLine) {
        TextFieldLineLimits.SingleLine
      } else {
        TextFieldLineLimits.MultiLine(maxHeightInLines = node.maxLines)
      }
    )
  }
}
