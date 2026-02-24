package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.LIVEWIRE_PORT
import com.r0adkll.livewire.LIVEWIRE_WS_PATH
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RootNode
import com.r0adkll.livewire.ui.data.LivewireUiJson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

enum class HostConnectionState {
  DISCONNECTED,
  FORWARDING,
  CONNECTING,
  CONNECTED,
  ERROR,
}

class LivewireHostConnection(
  vararg decoders: PayloadDecoder<*>,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(
    decoders: Collection<PayloadDecoder<*>>,
    context: CoroutineContext = Dispatchers.IO,
  ) : this(*decoders.toTypedArray(), context = context)

  private val scope = CoroutineScope(context + SupervisorJob())

  private val _connectionState = MutableStateFlow(HostConnectionState.DISCONNECTED)
  val connectionState: StateFlow<HostConnectionState> = _connectionState.asStateFlow()

  private val _incomingMessages = MutableSharedFlow<Any>(extraBufferCapacity = 64)
  val incomingMessages: SharedFlow<Any> = _incomingMessages.asSharedFlow()

  private val _incomingLayoutNodes = MutableStateFlow<LayoutNode>(RootNode())
  val incomingLayoutNodes: StateFlow<LayoutNode> = _incomingLayoutNodes.asStateFlow()

  private var client: HttpClient? = null
  private var activeDadb: dadb.Dadb? = null
  private var activeForwarder: AutoCloseable? = null
  private var activeJob: Job? = null
  var session: WebSocketSession? = null
    private set

  private val envelopeDecoder = EnvelopeDecoder(
    payloadDecoders = decoders.toSet()
  )

  fun connect(device: AdbDevice) {
    activeJob = scope.launch {
      try {
        _connectionState.value = HostConnectionState.FORWARDING
        activeDadb = device.connection
        activeForwarder = device.connection.tcpForward(LIVEWIRE_PORT, LIVEWIRE_PORT)

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
              when (frame) {
                is Frame.Text -> {
                  val text = frame.readText()
                  println("Frame -- $text")
                  val payload = envelopeDecoder.decode(text)
                  if (payload != null) {
                    println("Payload -- $payload")
                    _incomingMessages.tryEmit(payload)
                  }
                }

                is Frame.Binary -> {
                  val bytes = frame.readBytes()
                  val jsonText = bytes.toString(Charsets.UTF_8)
                  println(jsonText)
                  val layoutNode = LivewireUiJson.decodeFromString<LayoutNode>(jsonText)
                  _incomingLayoutNodes.emit(layoutNode)
                }

                else -> Unit
              }
            }
          } finally {
            session = null
            _connectionState.value = HostConnectionState.DISCONNECTED
            _incomingLayoutNodes.emit(RootNode())
          }
        }

      } catch (e: Exception) {
        e.printStackTrace()
        _connectionState.value = HostConnectionState.ERROR
      }
    }

    activeJob?.invokeOnCompletion {
      activeForwarder?.close()
      activeForwarder = null
      activeDadb?.close()
      activeDadb = null
    }
  }

  suspend inline fun <reified T> send(
    payload: T,
    json: Json = EnvelopeJson,
  ) {
    session?.send(Frame.Text(json.encodeToString(payload)))
  }

  fun disconnect() {
    scope.launch {
      session?.close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected"))
      client?.close()
      client = null
      activeForwarder?.close()
      activeForwarder = null
      activeDadb?.close()
      activeDadb = null
      _connectionState.value = HostConnectionState.DISCONNECTED
      _incomingLayoutNodes.emit(RootNode())
    }
  }

  fun close() {
    disconnect()
    scope.cancel()
  }
}
