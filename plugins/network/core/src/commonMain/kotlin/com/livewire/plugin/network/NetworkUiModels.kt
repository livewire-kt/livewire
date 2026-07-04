package com.livewire.plugin.network

import androidx.compose.runtime.Immutable
import com.livewire.plugin.network.data.NetworkEvent

@Immutable
data class NetworkUiState(
  val events: List<NetworkEvent>,
  val selectedEvent: NetworkEvent?,
  val filterText: String,
  val selectedDetailTab: Int,
  val eventSink: (NetworkUiEvent) -> Unit,
)

sealed interface NetworkUiEvent {
  data class SelectEvent(val event: NetworkEvent) : NetworkUiEvent
  data object ClearSelection : NetworkUiEvent
  data class UpdateFilter(val text: String) : NetworkUiEvent
  data object ClearAll : NetworkUiEvent
  data class SelectDetailTab(val index: Int) : NetworkUiEvent
}
