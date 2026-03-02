package com.r0adkll.livewire.client

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.transport.LivewireIncoming
import com.r0adkll.livewire.ui.transport.LivewireWebSocketCodec
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.WebSocketSession
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
  private val serializationStrategy: LayoutNodeSerializationStrategy,
  context: CoroutineContext = Dispatchers.IO,
) {
  constructor(
    decoders: Collection<PayloadDecoder<*>>,
    serializationStrategy: LayoutNodeSerializationStrategy,
    context: CoroutineContext = Dispatchers.IO,
  ) : this(
    *decoders.toTypedArray(),
    serializationStrategy = serializationStrategy,
    context = context,
  )

  private val scope = CoroutineScope(context + SupervisorJob())

  private val _connectionState = MutableStateFlow(ConnectionState.STOPPED)
  val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<Any>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<Any> = _incomingMessages.asSharedFlow()

  val outgoingLayoutSize: StateFlow<Long>
    field = MutableStateFlow(0L)

  private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
  var activeSession: WebSocketSession? = null
    private set

  val codec = LivewireWebSocketCodec(decoders.toSet())

  fun start() {
    if (server != null) {
      return
    }

    logDebug("Livewire", "Starting server on port ${LivewireConstants.Port}")
    server = embeddedServer(CIO, port = LivewireConstants.Port) {
      install(WebSockets)
      routing {
        webSocket(LivewireConstants.WsPath) {
          activeSession = this
          _connectionState.value = ConnectionState.CONNECTED
          try {
            for (frame in incoming) {
              when (val incomingMessage = codec.decode(frame)) {
                is LivewireIncoming.Payload -> _incomingMessages.tryEmit(incomingMessage.payload)
                is LivewireIncoming.Layout -> Unit
                null -> Unit
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

  suspend inline fun <reified T : Any> send(envelope: T) {
    activeSession?.send(codec.encodePayload(envelope))
  }

  suspend fun sendLayoutNode(node: LayoutNode) {
    activeSession?.send(codec.encodeLayout(node))
  }

  fun stop() {
    server?.stop(1000, 2000)
    server = null
    activeSession = null
    _connectionState.value = ConnectionState.STOPPED
    scope.cancel()
  }
}
