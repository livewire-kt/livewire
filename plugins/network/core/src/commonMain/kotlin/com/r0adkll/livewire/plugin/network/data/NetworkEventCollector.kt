package com.r0adkll.livewire.plugin.network.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object NetworkEventCollector {

  private const val MAX_EVENTS = 500

  private var idCounter = 0L

  private val _events = MutableStateFlow<List<NetworkEvent>>(emptyList())
  val events: StateFlow<List<NetworkEvent>> = _events.asStateFlow()

  fun recordRequest(request: NetworkRequest): String {
    val id = "net-${idCounter++}"
    val event = NetworkEvent(
      id = id,
      request = request,
    )
    _events.update { current ->
      (listOf(event) + current).take(MAX_EVENTS)
    }
    return id
  }

  fun recordResponse(id: String, response: NetworkResponse, durationMs: Long) {
    _events.update { current ->
      current.map { event ->
        if (event.id == id) {
          event.copy(
            response = response,
            durationMs = durationMs,
          )
        } else {
          event
        }
      }
    }
  }

  fun recordError(id: String, error: String, durationMs: Long) {
    _events.update { current ->
      current.map { event ->
        if (event.id == id) {
          event.copy(
            error = error,
            durationMs = durationMs,
          )
        } else {
          event
        }
      }
    }
  }

  fun clear() {
    _events.update { emptyList() }
  }
}
