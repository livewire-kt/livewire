package com.livewire.plugin.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.livewire.plugin.preferences.composables.AddEntryPanel
import com.livewire.plugin.preferences.composables.PreferenceEntryRow
import com.livewire.plugin.preferences.composables.PreferencesToolBar
import com.livewire.plugin.preferences.data.PreferenceValue
import com.livewire.plugin.preferences.data.PreferencesInspector
import com.livewire.plugin.preferences.data.StoreKind
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Column
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.CodeBlock
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.Text

@Composable
internal fun PreferencesPluginContent(inspector: PreferencesInspector) {
  val presenter = remember { PreferencesPresenter(inspector) }
  val state = presenter.present()

  Column(
    modifier = LivewireModifier.fillMaxSize(),
  ) {
    PreferencesToolBar(
      selectedStore = state.selectedStore,
      stores = state.stores,
      filterQuery = state.filterQuery,
      addEntryVisible = state.addEntry != null,
      pendingConfirm = state.pendingConfirm,
      eventSink = state.eventSink,
    )

    HorizontalDivider(LivewireModifier.fillMaxWidth())

    AddEntryPanel(
      state = state.addEntry,
      store = state.selectedStore,
      eventSink = state.eventSink,
    )

    val selected = state.selectedStore
    when {
      selected == null -> EmptyMessage("No preference stores found")

      selected.kind == StoreKind.ProtoDataStore -> {
        val content = state.entries.firstOrNull()
          ?.let { it.value as? PreferenceValue.OpaqueValue }
          ?.rendered
          .orEmpty()
        CodeBlock(
          content = content,
          searchable = true,
          modifier = LivewireModifier
            .weight(1f)
            .fillMaxWidth(),
        )
      }

      state.entries.isEmpty() -> EmptyMessage(
        if (state.filterQuery.isBlank()) "No entries" else "No keys match the filter",
      )

      else -> Column(
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(),
      ) {
        state.entries.forEach { entry ->
          // Keyed per preference so host-side editor state stays with the
          // logical row even when the sorted list shifts around it.
          key(entry.key) {
            PreferenceEntryRow(
              entry = entry,
              editing = state.editing?.takeIf { it.key == entry.key },
              pendingDelete = (state.pendingConfirm as? PendingConfirm.DeleteKey)?.key == entry.key,
              editable = selected.editable,
              eventSink = state.eventSink,
            )
            HorizontalDivider(LivewireModifier.fillMaxWidth())
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyMessage(message: String) {
  Box(
    modifier = LivewireModifier
      .fillMaxWidth()
      .padding(32.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      style = LivewireTheme.typography.bodyMedium,
      color = LivewireTheme.colorScheme.onSurfaceVariant,
    )
  }
}
