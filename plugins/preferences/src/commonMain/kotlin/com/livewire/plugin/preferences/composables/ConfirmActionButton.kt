package com.livewire.plugin.preferences.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.livewire.plugin.preferences.ui.Close
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.ui.actions.clickAction
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.width
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Text

/**
 * Two-step confirmation for destructive actions: the icon button swaps to a
 * "Confirm?" button (plus cancel) until confirmed or cancelled.
 */
@Composable
internal fun ConfirmActionButton(
  icon: ImageVector,
  pending: Boolean,
  onRequest: () -> Unit,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
) {
  if (pending) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier,
    ) {
      Button(
        action = clickAction { onConfirm() },
        size = ButtonSize.ExtraSmall,
        style = ButtonStyle.Tonal,
      ) {
        Text("Confirm?")
      }
      Spacer(LivewireModifier.width(4.dp))
      IconButton(
        action = clickAction { onCancel() },
        size = ButtonSize.ExtraSmall,
      ) {
        Icon(Icons.Close)
      }
    }
  } else {
    IconButton(
      action = clickAction { onRequest() },
      enabled = enabled,
      modifier = modifier,
    ) {
      Icon(icon)
    }
  }
}
