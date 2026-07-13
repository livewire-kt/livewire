package com.livewire.plugin.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.livewire.plugin.preferences.data.PreferenceEntry
import com.livewire.plugin.preferences.data.PreferenceStore
import com.livewire.plugin.preferences.data.PreferenceValue
import com.livewire.plugin.preferences.data.PreferenceValueType
import com.livewire.plugin.preferences.data.PreferencesInspector
import com.livewire.plugin.preferences.data.parsePreferenceValue
import com.livewire.plugin.preferences.data.render
import com.livewire.plugin.preferences.data.toStoreInfo
import com.livewire.plugin.preferences.data.type
import kotlinx.coroutines.launch

class PreferencesPresenter(
  private val inspector: PreferencesInspector,
) {

  private val stores = mutableStateListOf<PreferenceStore>()
  private var selectedStoreId by mutableStateOf<String?>(null)
  private val entries = mutableStateListOf<PreferenceEntry>()
  private var filterQuery by mutableStateOf("")
  private var editing by mutableStateOf<EditingState?>(null)
  private var addEntry by mutableStateOf<AddEntryState?>(null)
  private var pendingConfirm by mutableStateOf<PendingConfirm?>(null)

  @Composable
  fun present(): PreferencesUiState {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
      refreshStores()
    }

    val selected = stores.find { it.id == selectedStoreId }

    // The live subscription follows the selected store. Inspectors return
    // stable instances per id, so this only restarts on real selection
    // changes.
    LaunchedEffect(selected) {
      entries.clear()
      selected?.entries()?.collect { latest ->
        entries.clear()
        entries.addAll(latest)
      }
    }

    return PreferencesUiState(
      stores = stores.map { it.toStoreInfo() },
      selectedStore = selected?.toStoreInfo(),
      entries = entries.filter {
        filterQuery.isBlank() || it.key.contains(filterQuery.trim(), ignoreCase = true)
      },
      filterQuery = filterQuery,
      editing = editing,
      addEntry = addEntry,
      pendingConfirm = pendingConfirm,
    ) { event ->
      when (event) {
        PreferencesUiEvent.Refresh -> scope.launch { refreshStores() }

        is PreferencesUiEvent.SelectStore -> {
          selectedStoreId = event.id
          filterQuery = ""
          editing = null
          addEntry = null
          pendingConfirm = null
        }

        is PreferencesUiEvent.FilterChange -> filterQuery = event.query

        is PreferencesUiEvent.StartEdit -> {
          pendingConfirm = null
          val entry = entries.find { it.key == event.key }
          val type = entry?.value?.type
          if (entry != null && type != null) {
            editing = EditingState(key = entry.key, type = type, draft = entry.value.render())
          }
        }

        // Draft edits only touch presenter state; the store is written on
        // CommitEdit, so live re-emissions never clobber in-progress typing.
        is PreferencesUiEvent.UpdateEditDraft -> {
          editing = editing?.copy(draft = event.text, error = null)
        }

        PreferencesUiEvent.CommitEdit -> {
          val current = editing
          if (current != null && selected != null) {
            parsePreferenceValue(current.type, current.draft)
              .onSuccess { value ->
                scope.launch {
                  selected.put(current.key, value)
                    .onSuccess { editing = null }
                    .onFailure { editing = current.copy(error = it.message) }
                }
              }
              .onFailure { editing = current.copy(error = it.message) }
          }
        }

        PreferencesUiEvent.CancelEdit -> editing = null

        is PreferencesUiEvent.ToggleBoolean -> {
          if (selected != null) {
            scope.launch {
              selected.put(event.key, PreferenceValue.BooleanValue(event.checked))
            }
          }
        }

        is PreferencesUiEvent.RequestDelete -> pendingConfirm = PendingConfirm.DeleteKey(event.key)

        PreferencesUiEvent.RequestClear -> pendingConfirm = PendingConfirm.ClearStore

        PreferencesUiEvent.ConfirmPending -> {
          val confirm = pendingConfirm
          pendingConfirm = null
          if (confirm != null && selected != null) {
            scope.launch {
              when (confirm) {
                is PendingConfirm.DeleteKey -> selected.remove(confirm.key)
                PendingConfirm.ClearStore -> selected.clear()
              }
            }
          }
        }

        PreferencesUiEvent.CancelPending -> pendingConfirm = null

        PreferencesUiEvent.ShowAddEntry -> {
          addEntry = AddEntryState(
            type = selected?.supportedTypes?.firstOrNull() ?: PreferenceValueType.String,
          )
        }

        PreferencesUiEvent.HideAddEntry -> addEntry = null

        is PreferencesUiEvent.UpdateAddEntry -> addEntry = event.state

        PreferencesUiEvent.CommitAddEntry -> {
          val current = addEntry
          if (current != null && selected != null) {
            if (current.key.isBlank()) {
              addEntry = current.copy(error = "Key cannot be blank")
            } else {
              parsePreferenceValue(current.type, current.draft)
                .onSuccess { value ->
                  scope.launch {
                    selected.put(current.key.trim(), value)
                      .onSuccess { addEntry = null }
                      .onFailure { addEntry = current.copy(error = it.message) }
                  }
                }
                .onFailure { addEntry = current.copy(error = it.message) }
            }
          }
        }
      }
    }
  }

  private suspend fun refreshStores() {
    inspector.discoverStores()
      .onSuccess { discovered ->
        stores.clear()
        stores.addAll(discovered)
        if (stores.none { it.id == selectedStoreId }) {
          selectedStoreId = stores.firstOrNull()?.id
        }
      }
      .onFailure { it.printStackTrace() }
  }
}
