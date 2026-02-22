package com.r0adkll.livewire

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class DataConnector<HostT : HostEvent, ClientT : ClientEvent> {

  var isConnected by mutableStateOf(false)

  private val _events = MutableSharedFlow<ClientT>(replay = 0)
  var hostSink: (HostT) -> Unit = {}

  suspend fun emitEvent(event: ClientT) {
    _events.emit(event)
  }

  @Composable
  abstract fun Connect()

  @Composable
  protected fun onEvent(block: (ClientT) -> Unit) {
    val sink = rememberUpdatedState(block)
    LaunchedEffect(Unit) {
      _events.collect { sink.value(it) }
    }
  }
}
