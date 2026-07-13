package com.livewire.plugin.preferences.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.livewire.plugin.preferences.AddEntryState
import com.livewire.plugin.preferences.PreferencesUiEvent
import com.livewire.plugin.preferences.data.PreferenceValueType
import com.livewire.plugin.preferences.data.StoreInfo
import com.livewire.plugin.preferences.ui.DropdownArrow
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.ui.actions.checkedChangeAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.DropdownMenu
import com.livewire.ui.widget.DropdownMenuItem
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Switch
import com.livewire.ui.widget.Text

@Composable
internal fun AddEntryPanel(
  state: AddEntryState?,
  store: StoreInfo?,
  eventSink: (PreferencesUiEvent) -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  if (state == null || store == null) return

  Surface(
    tonalElevation = 2.dp,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Row(
        modifier = LivewireModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        CompactTextField(
          initialValue = state.key,
          onValueChange = valueChangeAction {
            eventSink(PreferencesUiEvent.UpdateAddEntry(state.copy(key = it, error = null)))
          },
          placeholder = "Key",
          onSubmit = clickAction { eventSink(PreferencesUiEvent.CommitAddEntry) },
          onCancel = clickAction { eventSink(PreferencesUiEvent.HideAddEntry) },
          modifier = LivewireModifier.weight(1f),
        )

        Spacer(LivewireModifier.width(16.dp))

        var typeMenuExpanded by remember { mutableStateOf(false) }
        Surface(
          shape = RoundedCornerShape(8.dp),
          tonalElevation = 2.dp,
          onClick = clickAction {
            typeMenuExpanded = true
          },
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = LivewireModifier.padding(horizontal = 12.dp, vertical = 8.dp),
          ) {
            Text(
              text = state.type.name,
              style = LivewireTheme.typography.labelLarge,
            )
            Spacer(LivewireModifier.width(8.dp))
            Icon(
              imageVector = Icons.DropdownArrow,
              modifier = LivewireModifier.size(24.dp),
            )
          }

          DropdownMenu(
            expanded = typeMenuExpanded,
            onDismissRequest = clickAction {
              typeMenuExpanded = false
            },
          ) {
            store.supportedTypes.forEach { type ->
              DropdownMenuItem(
                text = type.name,
                onClick = clickAction {
                  eventSink(
                    PreferencesUiEvent.UpdateAddEntry(
                      state.copy(
                        type = type,
                        draft = if (type == PreferenceValueType.Boolean) "false" else "",
                        error = null,
                      ),
                    ),
                  )
                  typeMenuExpanded = false
                },
              )
            }
          }
        }
      }

      Spacer(LivewireModifier.height(8.dp))

      // Keyed by type so the host-side text state reseeds when the type
      // (and therefore the client-side draft) resets.
      key(state.type) {
        if (state.type == PreferenceValueType.Boolean) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Value",
              style = LivewireTheme.typography.labelLarge,
            )
            Spacer(LivewireModifier.width(16.dp))
            Switch(
              checked = state.draft.toBoolean(),
              onCheckedChange = checkedChangeAction {
                eventSink(PreferencesUiEvent.UpdateAddEntry(state.copy(draft = it.toString())))
              },
            )
          }
        } else {
          CompactTextField(
            initialValue = state.draft,
            onValueChange = valueChangeAction {
              eventSink(PreferencesUiEvent.UpdateAddEntry(state.copy(draft = it, error = null)))
            },
            placeholder = when (state.type) {
              PreferenceValueType.StringSet -> "Value — one entry per line"
              PreferenceValueType.Bytes -> "Value — Base64"
              else -> "Value"
            },
            singleLine = state.type != PreferenceValueType.StringSet,
            onSubmit = clickAction { eventSink(PreferencesUiEvent.CommitAddEntry) },
            onCancel = clickAction { eventSink(PreferencesUiEvent.HideAddEntry) },
            modifier = LivewireModifier.fillMaxWidth(),
          )
        }
      }

      state.error?.let { error ->
        Spacer(LivewireModifier.height(4.dp))
        Text(
          text = error,
          style = LivewireTheme.typography.labelSmall,
          color = LivewireTheme.colorScheme.error,
        )
      }

      Spacer(LivewireModifier.height(8.dp))

      Row {
        Button(
          action = clickAction {
            eventSink(PreferencesUiEvent.CommitAddEntry)
          },
        ) {
          Text("Add")
        }
        Spacer(LivewireModifier.width(8.dp))
        Button(
          action = clickAction {
            eventSink(PreferencesUiEvent.HideAddEntry)
          },
          style = ButtonStyle.Tonal,
        ) {
          Text("Cancel")
        }
      }
    }
  }
}
