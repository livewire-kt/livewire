package com.r0adkll.livewire.client

import android.util.Log
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
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
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

enum class ConnectionState {
  STOPPED,
  STARTED,
  CONNECTED,
  ERROR,
}

class LivewireServer(
  vararg decoders: PayloadDecoder,
) {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val _connectionState = MutableStateFlow(ConnectionState.STOPPED)
  val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<Payload>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<Payload> = _incomingMessages.asSharedFlow()

  private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
  var activeSession: WebSocketSession? = null
    private set

  private val envelopeDecoder = EnvelopeDecoder(
    payloadDecoders = decoders.toSet()
  )

  fun start() {
    server = embeddedServer(CIO, port = LIVEWIRE_PORT) {
      install(WebSockets)
      routing {
        webSocket(LIVEWIRE_WS_PATH) {
          activeSession = this
          _connectionState.value = ConnectionState.CONNECTED
          try {
            for (frame in incoming) {
              if (frame is Frame.Text) {
                val text = frame.readText()
                val payload = envelopeDecoder.decode(text)
                if (payload != null) {
                  _incomingMessages.tryEmit(payload)
                }

                // Echo back for initial testing
                val echo = Envelope(SimpleMessage("pong!"))
                send(Frame.Text(echo.toJsonString()))
              }
            }
          } catch (e: Exception) {
            e.printStackTrace()
          } finally {
            activeSession = null
            _connectionState.value = ConnectionState.STARTED
          }
        }
      }
    }.also {
      it.start(wait = false)
      _connectionState.value = ConnectionState.STARTED
    }
  }

  suspend inline fun <reified T> send(envelope: Envelope<T>) {
    activeSession?.send(Frame.Text(envelope.toJsonString()))
  }

  fun stop() {
    server?.stop(1000, 2000)
    server = null
    activeSession = null
    _connectionState.value = ConnectionState.STOPPED
    scope.cancel()
  }
}
