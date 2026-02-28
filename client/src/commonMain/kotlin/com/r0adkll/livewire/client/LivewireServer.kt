package com.r0adkll.livewire.client

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.data.LivewireUiJson
import com.r0adkll.livewire.ui.layout.LayoutNode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.utils.io.core.toByteArray
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.CoroutineContext

enum class ConnectionState {
  STOPPED,
  STARTED,
  CONNECTED,
  ERROR,
}

class LivewireServer(
  vararg decoders: PayloadDecoder<*>,
  context: CoroutineContext = Dispatchers.IO,
) {
  constructor(
    decoders: Collection<PayloadDecoder<*>>,
    context: CoroutineContext = Dispatchers.IO,
  ) : this(*decoders.toTypedArray(), context = context)

  private val scope = CoroutineScope(context + SupervisorJob())

  private val _connectionState = MutableStateFlow(ConnectionState.STOPPED)
  val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<Any>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<Any> = _incomingMessages.asSharedFlow()

  private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
  var activeSession: WebSocketSession? = null
    private set

  private val envelopeDecoder = EnvelopeDecoder(
    payloadDecoders = decoders.toSet()
  )

  fun start() {
    if (server != null) {
      return
    }

    server = embeddedServer(CIO, port = LivewireConstants.Port) {
      install(WebSockets)
      routing {
        webSocket(LivewireConstants.WsPath) {
          activeSession = this
          _connectionState.value = ConnectionState.CONNECTED
          try {
            for (frame in incoming) {
              if (frame is Frame.Text) {
                val text = frame.readText()
                val payload = envelopeDecoder.decode(text)
                if (payload != null) {
                  _incomingMessages.tryEmit(payload)
                } else {
                  logDebug("Livewire", "Unknown message: $text")
                }
              }
            }
          } catch (e: Exception) {
            logError("Livewire", "Server websocket error", e)
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

  suspend inline fun <reified T> send(envelope: T) {
    val envelopeJson = EnvelopeJson.encodeToString(envelope)
    logDebug("Livewire", "Sending $envelopeJson")
    activeSession?.send(Frame.Text(envelopeJson))
  }

  suspend fun sendLayoutNode(node: LayoutNode) {
    val nodeBinary = LivewireUiJson.encodeToString(node)
    logDebug("Livewire", "Layout: $nodeBinary")
    activeSession?.send(
      Frame.Binary(true, nodeBinary.toByteArray())
    )
  }

  fun stop() {
    server?.stop(1000, 2000)
    server = null
    activeSession = null
    _connectionState.value = ConnectionState.STOPPED
    scope.cancel()
  }
}
