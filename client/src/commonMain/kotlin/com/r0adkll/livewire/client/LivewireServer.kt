package com.r0adkll.livewire.client

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.crypto.LivewireHandshake
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.composition.LivewireOutput
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.transport.LivewireIncoming
import com.r0adkll.livewire.ui.transport.LivewireWebSocketCodec
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readBytes
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState {
  Stopped,
  Connecting,
  Connected,
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

  val connectionState: StateFlow<ConnectionState>
    field = MutableStateFlow(Stopped)

  val incomingMessages: SharedFlow<Any>
    field = MutableSharedFlow<Any>(extraBufferCapacity = 64)

  val outgoingLayoutSize: StateFlow<Long>
    field = MutableStateFlow(0L)

  var activeSession: WebSocketSession? = null
    private set

  var onConnected: (suspend LivewireServer.() -> Unit)? = null

  private val httpClient = HttpClient(createPlatformEngine()) {
    install(WebSockets) {
      pingInterval = 15.seconds
    }
  }

  val codec = LivewireWebSocketCodec(
    decoders = decoders.toSet(),
    serializationStrategy = serializationStrategy,
    outgoingSizeReporter = { bytes ->
      outgoingLayoutSize.value = bytes
    },
  )

  fun start() {
    if (connectionState.value != Stopped) return

    logDebug("Livewire", "Starting client connection loop")
    scope.launch { connectionLoop() }
  }

  private suspend fun connectionLoop() {
    while (true) {
      connectionState.value = Connecting
      logDebug("Livewire", "Attempting connection to 127.0.0.1:${LivewireConstants.Port}")

      try {
        httpClient.webSocket(
          host = "127.0.0.1",
          port = LivewireConstants.Port,
          path = LivewireConstants.WsPath,
          request = { url.parameters.append("connection_id", connectionId) },
        ) {
          logDebug("Livewire", "Performing encryption handshake…")
          codec.secureSession = LivewireHandshake().perform(
            sendBytes = { bytes -> send(Frame.Binary(true, bytes)) },
            receiveBytes = { (incoming.receive() as Frame.Binary).readBytes() },
          )
          logDebug("Livewire", "Encryption handshake complete")

          activeSession = this

          connectionState.value = Connected
          logDebug("Livewire", "Connected")
          try {
            onConnected?.invoke(this@LivewireServer)
            for (frame in incoming) {
              when (val incomingMessage = codec.decode(frame)) {
                is LivewireIncoming.Payload -> incomingMessages.tryEmit(incomingMessage.payload)
                is LivewireIncoming.Layout -> Unit
                is LivewireIncoming.Patches -> Unit
                null -> Unit
              }
            }
          } catch (e: CancellationException) {
            throw e
          } catch (e: Exception) {
            logError("Livewire", "WebSocket error", e)
          } finally {
            codec.secureSession = null
            activeSession = null
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logDebug("Livewire", "Connection failed: ${e.message}")
      }

      connectionState.value = Connecting
      logDebug("Livewire", "Reconnecting in ${ReconnectDelay}ms")
      delay(ReconnectDelay)
    }
  }

  suspend inline fun <reified T : Any> send(envelope: T) {
    try {
      activeSession?.send(codec.encodePayload(envelope))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logDebug("Livewire", "Failed to send payload: ${e.message}")
    }
  }

  suspend fun sendLayoutNode(node: LayoutNode) {
    try {
      activeSession?.send(codec.encodeLayout(node))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logDebug("Livewire", "Failed to send layout node: ${e.message}")
    }
  }

  suspend fun sendLayout(output: LivewireOutput) {
    try {
      when (output) {
        is LivewireOutput.FullTree -> activeSession?.send(codec.encodeLayout(output.root))
        is LivewireOutput.Patches -> activeSession?.send(codec.encodePatches(output.patches))
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logDebug("Livewire", "Failed to send layout: ${e.message}")
    }
  }

  fun stop() {
    activeSession = null
    connectionState.value = Stopped
    httpClient.close()
    scope.cancel()
  }
}

private const val ReconnectDelay = 3000L
