package com.livewire.plugin.preferences.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.livewire.plugin.preferences.PendingConfirm
import com.livewire.plugin.preferences.PreferencesUiEvent
import com.livewire.plugin.preferences.data.StoreInfo
import com.livewire.plugin.preferences.ui.Add
import com.livewire.plugin.preferences.ui.Delete
import com.livewire.plugin.preferences.ui.DropdownArrow
import com.livewire.plugin.preferences.ui.Icons
import com.livewire.plugin.preferences.ui.Refresh
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.width
import com.livewire.ui.widget.DropdownMenu
import com.livewire.ui.widget.DropdownMenuItem
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextStyle

@Composable
internal fun PreferencesToolBar(
  selectedStore: StoreInfo?,
  stores: List<StoreInfo>,
  filterQuery: String,
  addEntryVisible: Boolean,
  pendingConfirm: PendingConfirm?,
  eventSink: (PreferencesUiEvent) -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      var menuExpanded by remember { mutableStateOf(false) }
      Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        onClick = clickAction {
          menuExpanded = true
        },
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = LivewireModifier.padding(horizontal = 12.dp),
        ) {
          Text(
            text = selectedStore?.name ?: "<no stores>",
            style = TextStyle.TitleMedium,
            modifier = LivewireModifier.padding(vertical = 6.dp),
          )
          Spacer(LivewireModifier.width(8.dp))
          Icon(
            imageVector = Icons.DropdownArrow,
            modifier = LivewireModifier.size(24.dp),
          )
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = clickAction {
            menuExpanded = false
          },
        ) {
          stores.forEach { store ->
            DropdownMenuItem(
              text = "${store.name} · ${store.kind.label}",
              onClick = clickAction {
                eventSink(PreferencesUiEvent.SelectStore(store.id))
                menuExpanded = false
              },
            )
          }
        }
      }

      Spacer(LivewireModifier.width(16.dp))

      // Remounting per store gives the filter field a fresh host-side state
      // when the selection changes (the presenter resets the query too).
      key(selectedStore?.id) {
        CompactTextField(
          initialValue = filterQuery,
          onValueChange = valueChangeAction {
            eventSink(PreferencesUiEvent.FilterChange(it))
          },
          placeholder = "Filter keys…",
          modifier = LivewireModifier.weight(1f),
        )
      }

      Spacer(LivewireModifier.width(16.dp))

      IconButton(
        action = clickAction {
          eventSink(PreferencesUiEvent.Refresh)
        },
      ) {
        Icon(Icons.Refresh)
      }

      IconButton(
        action = clickAction {
          eventSink(
            if (addEntryVisible) {
              PreferencesUiEvent.HideAddEntry
            } else {
              PreferencesUiEvent.ShowAddEntry
            },
          )
        },
        enabled = selectedStore?.editable == true,
      ) {
        Icon(Icons.Add)
      }

      ConfirmActionButton(
        icon = Icons.Delete,
        pending = pendingConfirm == PendingConfirm.ClearStore,
        enabled = selectedStore?.editable == true,
        onRequest = { eventSink(PreferencesUiEvent.RequestClear) },
        onConfirm = { eventSink(PreferencesUiEvent.ConfirmPending) },
        onCancel = { eventSink(PreferencesUiEvent.CancelPending) },
      )
    }
  }
}
