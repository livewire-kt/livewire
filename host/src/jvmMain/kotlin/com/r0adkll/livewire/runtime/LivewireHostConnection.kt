package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.livewire.LIVEWIRE_PORT
import com.r0adkll.livewire.livewire.LIVEWIRE_WS_PATH
import com.r0adkll.livewire.protocol.Envelope
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.protocol.Payload
import com.r0adkll.livewire.protocol.SimpleMessage
import com.r0adkll.livewire.protocol.payloadEnvelopeFromJsonString
import com.r0adkll.livewire.protocol.toJsonString
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class HostConnectionState {
  DISCONNECTED,
  FORWARDING,
  CONNECTING,
  CONNECTED,
  ERROR,
}

class LivewireHostConnection(
  vararg decoders: PayloadDecoder,
  context: CoroutineContext = Dispatchers.IO,
) {

  private val scope = CoroutineScope(context + SupervisorJob())

  private val _connectionState = MutableStateFlow(HostConnectionState.DISCONNECTED)
  val connectionState: StateFlow<HostConnectionState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<Payload>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<Payload> = _incomingMessages.asSharedFlow()

  private var client: HttpClient? = null
  var session: WebSocketSession? = null
    private set

  private val envelopeDecoder = EnvelopeDecoder(
    payloadDecoders = decoders.toSet()
  )

  fun connect() {
    scope.launch {
      try {
        _connectionState.value = HostConnectionState.FORWARDING
        AdbForwarder.forward(LIVEWIRE_PORT).getOrThrow()

        _connectionState.value = HostConnectionState.CONNECTING
        val httpClient = HttpClient(CIO) {
          install(WebSockets)
        }
        client = httpClient

        httpClient.webSocket(
          host = "localhost",
          port = LIVEWIRE_PORT,
          path = LIVEWIRE_WS_PATH,
        ) {
          session = this
          _connectionState.value = HostConnectionState.CONNECTED
          try {
            for (frame in incoming) {
              if (frame is Frame.Text) {
                val text = frame.readText()
                val payload = envelopeDecoder.decode(text)
                if (payload != null) {
                  _incomingMessages.tryEmit(payload)
                }
              }
            }
          } finally {
            session = null
            _connectionState.value = HostConnectionState.DISCONNECTED
          }
        }
      } catch (e: Exception) {
        _connectionState.value = HostConnectionState.ERROR
      }
    }
  }

  suspend inline fun <reified T> send(envelope: Envelope<T>) {
    session?.send(Frame.Text(envelope.toJsonString()))
  }

  fun disconnect() {
    scope.launch {
      session?.close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
      client?.close()
      client = null
      AdbForwarder.removeForward(LIVEWIRE_PORT)
      _connectionState.value = HostConnectionState.DISCONNECTED
    }
  }

  fun close() {
    disconnect()
    scope.cancel()
  }
}
