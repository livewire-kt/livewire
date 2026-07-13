package com.livewire.plugin.preferences.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.BasicTextField
import com.livewire.ui.widget.Surface

/**
 * The compact text field used across the plugin, matching the network
 * plugin's search bar styling.
 */
@Composable
internal fun CompactTextField(
  initialValue: String,
  onValueChange: ValueChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  placeholder: String? = null,
  singleLine: Boolean = true,
  onSubmit: ClickAction? = null,
  onCancel: ClickAction? = null,
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    tonalElevation = 2.dp,
    modifier = modifier,
  ) {
    BasicTextField(
      initialValue = initialValue,
      onValueChange = onValueChange,
      placeholder = placeholder,
      singleLine = singleLine,
      textStyle = LivewireTheme.typography.bodyMedium,
      onSubmit = onSubmit,
      onCancel = onCancel,
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(12.dp),
    )
  }
}
