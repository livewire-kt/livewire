package com.livewire.plugin.preferences.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.livewire.plugin.preferences.EditingState
import com.livewire.plugin.preferences.PreferencesUiEvent
import com.livewire.plugin.preferences.data.PreferenceEntry
import com.livewire.plugin.preferences.data.PreferenceValue
import com.livewire.plugin.preferences.data.PreferenceValueType
import com.livewire.plugin.preferences.data.editable
import com.livewire.plugin.preferences.data.render
import com.livewire.plugin.preferences.data.type
import com.livewire.plugin.preferences.ui.Check
import com.livewire.plugin.preferences.ui.Close
import com.livewire.plugin.preferences.ui.Delete
import com.livewire.plugin.preferences.ui.Edit
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.ui.actions.checkedChangeAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.copyClickable
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Switch
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextStyle

private const val MaxDisplayLength = 120

@Composable
internal fun PreferenceEntryRow(
  entry: PreferenceEntry,
  editing: EditingState?,
  pendingDelete: Boolean,
  editable: Boolean,
  eventSink: (PreferencesUiEvent) -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Row(
      modifier = LivewireModifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = LivewireModifier.weight(1f),
      ) {
        Text(
          text = entry.key,
          style = TextStyle.TitleSmall,
          modifier = LivewireModifier.copyClickable(entry.key),
        )
        Text(
          text = entry.value.type?.name ?: "opaque",
          style = TextStyle.LabelSmall,
          color = LivewireTheme.colorScheme.onSurfaceVariant,
        )
      }

      Spacer(LivewireModifier.width(16.dp))

      val value = entry.value
      when {
        editing != null -> {
          CompactTextField(
            initialValue = editing.draft,
            onValueChange = valueChangeAction {
              eventSink(PreferencesUiEvent.UpdateEditDraft(it))
            },
            singleLine = editing.type != PreferenceValueType.StringSet,
            onSubmit = clickAction { eventSink(PreferencesUiEvent.CommitEdit) },
            onCancel = clickAction { eventSink(PreferencesUiEvent.CancelEdit) },
            modifier = LivewireModifier.weight(2f),
          )
          IconButton(
            action = clickAction {
              eventSink(PreferencesUiEvent.CommitEdit)
            },
          ) {
            Icon(Icons.Check)
          }
          IconButton(
            action = clickAction {
              eventSink(PreferencesUiEvent.CancelEdit)
            },
          ) {
            Icon(Icons.Close)
          }
        }

        value is PreferenceValue.BooleanValue -> {
          Switch(
            checked = value.value,
            onCheckedChange = checkedChangeAction(key = entry.key) {
              eventSink(PreferencesUiEvent.ToggleBoolean(entry.key, it))
            },
            enabled = editable,
          )
        }

        else -> {
          Text(
            text = value.render().toDisplayText(),
            style = TextStyle.BodyMedium,
            modifier = LivewireModifier
              .weight(2f)
              .copyClickable(value.render()),
          )
          if (editable && value.editable) {
            IconButton(
              action = clickAction {
                eventSink(PreferencesUiEvent.StartEdit(entry.key))
              },
            ) {
              Icon(Icons.Edit)
            }
          }
        }
      }

      if (editable && editing == null) {
        ConfirmActionButton(
          icon = Icons.Delete,
          pending = pendingDelete,
          onRequest = { eventSink(PreferencesUiEvent.RequestDelete(entry.key)) },
          onConfirm = { eventSink(PreferencesUiEvent.ConfirmPending) },
          onCancel = { eventSink(PreferencesUiEvent.CancelPending) },
        )
      }
    }

    editing?.error?.let { error ->
      Text(
        text = error,
        style = TextStyle.LabelSmall,
        color = LivewireTheme.colorScheme.error,
      )
    }
  }
}

private fun String.toDisplayText(): String {
  val singleLine = replace("\n", " · ")
  return if (singleLine.length > MaxDisplayLength) {
    singleLine.take(MaxDisplayLength) + "…"
  } else {
    singleLine
  }
}
