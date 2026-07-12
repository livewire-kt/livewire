package com.livewire.plugin.preferences

import androidx.compose.runtime.Immutable
import com.livewire.plugin.preferences.data.PreferenceEntry
import com.livewire.plugin.preferences.data.PreferenceValueType
import com.livewire.plugin.preferences.data.StoreInfo

@Immutable
data class PreferencesUiState(
  val stores: List<StoreInfo>,
  val selectedStore: StoreInfo?,
  val entries: List<PreferenceEntry>,
  val filterQuery: String,
  val editing: EditingState?,
  val addEntry: AddEntryState?,
  val pendingConfirm: PendingConfirm?,
  val eventSink: (PreferencesUiEvent) -> Unit,
)

@Immutable
data class EditingState(
  val key: String,
  val type: PreferenceValueType,
  val draft: String,
  val error: String? = null,
)

@Immutable
data class AddEntryState(
  val key: String = "",
  val type: PreferenceValueType = PreferenceValueType.String,
  val draft: String = "",
  val error: String? = null,
)

sealed interface PendingConfirm {
  data class DeleteKey(val key: String) : PendingConfirm
  data object ClearStore : PendingConfirm
}

sealed interface PreferencesUiEvent {
  data object Refresh : PreferencesUiEvent
  data class SelectStore(val id: String) : PreferencesUiEvent
  data class FilterChange(val query: String) : PreferencesUiEvent

  data class StartEdit(val key: String) : PreferencesUiEvent
  data class UpdateEditDraft(val text: String) : PreferencesUiEvent
  data object CommitEdit : PreferencesUiEvent
  data object CancelEdit : PreferencesUiEvent
  data class ToggleBoolean(val key: String, val checked: Boolean) : PreferencesUiEvent

  data class RequestDelete(val key: String) : PreferencesUiEvent
  data object RequestClear : PreferencesUiEvent
  data object ConfirmPending : PreferencesUiEvent
  data object CancelPending : PreferencesUiEvent

  data object ShowAddEntry : PreferencesUiEvent
  data object HideAddEntry : PreferencesUiEvent
  data class UpdateAddEntry(val state: AddEntryState) : PreferencesUiEvent
  data object CommitAddEntry : PreferencesUiEvent
}
