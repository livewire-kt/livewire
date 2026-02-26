package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.runtime.devicemanager.AdbDevice
import com.r0adkll.livewire.runtime.devicemanager.HostDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceInfo
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceManager
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceType
import com.r0adkll.livewire.transport.EnvelopeDecoder
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.data.LivewireUiJson
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RootNode
import dadb.Dadb
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
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

  private var activeConnection: ActiveConnection? = null

  var session: WebSocketSession? = null
    private set

  private val envelopeDecoder = EnvelopeDecoder(
    payloadDecoders = decoders.toSet()
  )

  suspend fun connect(device: HostDevice) {
    println("connect to $device")
    disconnect()

    when (device) {
      is AdbDevice -> connectAndroid(device)
      is IosDeviceInfo -> connectIos(device)
    }
  }

  private fun connectAndroid(device: AdbDevice) {
    scope.launch {
      try {
        _connectionState.value = HostConnectionState.FORWARDING
        activeConnection = ActiveConnection.AndroidConnection(
          dadb = device.connection,
          forwarder = device.connection.tcpForward(LivewireConstants.Port, LivewireConstants.Port),
          socketJob = startClientConnection(),
        )
      } catch (e: Exception) {
        e.printStackTrace()
        _connectionState.value = HostConnectionState.ERROR
      }
    }
  }

  private fun connectIos(device: IosDeviceInfo) {
    when (device.deviceType) {
      IosDeviceType.Simulator -> connectIosSimulator()
      IosDeviceType.Physical -> connectIosPhysical(device)
    }
  }

  private fun connectIosSimulator() {
    scope.launch {
      try {
        activeConnection = ActiveConnection.IosConnection.Simulator(startClientConnection())
      } catch (e: Exception) {
        e.printStackTrace()
        _connectionState.value = HostConnectionState.ERROR
      }
    }
  }

  private fun connectIosPhysical(device: IosDeviceInfo) {
    scope.launch {
      try {
        _connectionState.value = HostConnectionState.FORWARDING
        val ok = IosDeviceManager.activate(device.udid)
        if (!ok) {
          _connectionState.value = HostConnectionState.ERROR
          return@launch
        }
        activeConnection = ActiveConnection.IosConnection.Physical(startClientConnection())
      } catch (e: Exception) {
        e.printStackTrace()
        _connectionState.value = HostConnectionState.ERROR
      }
    }
  }

  private fun startClientConnection(): Job {
    _connectionState.value = HostConnectionState.CONNECTING

    val httpClient = HttpClient(CIO) {
      install(WebSockets)
    }

    val job = scope.launch {
      httpClient.webSocket(
        host = "localhost",
        port = LivewireConstants.Port,
        path = LivewireConstants.WsPath,
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
                val jsonText = bytes.decodeToString()
                val layoutNode = LivewireUiJson.decodeFromString<LayoutNode>(jsonText)
                _incomingLayoutNodes.emit(layoutNode)
              }

              else -> Unit
            }
          }
        } finally {
          disconnect()
        }
      }
    }

    job.invokeOnCompletion {
      httpClient.close()
    }

    return job
  }

  suspend inline fun <reified T> send(payload: T) {
    val json = when (payload) {
      is UiProtocol -> EnvelopeJson.encodeToString(UiProtocol.serializer(), payload)
      is LivewireAction -> EnvelopeJson.encodeToString(LivewireAction.serializer(), payload)
      else -> EnvelopeJson.encodeToString(payload)
    }
    session?.send(Frame.Text(json))
  }

  suspend fun disconnect() {
    session?.close()
    session = null

    activeConnection?.let { connection ->
      scope.launch {
        connection.close()
      }
    }
    activeConnection = null

    _connectionState.value = HostConnectionState.DISCONNECTED
    _incomingLayoutNodes.value = RootNode()
  }

  suspend fun close() {
    disconnect()
    scope.cancel()
  }
}

sealed interface ActiveConnection {
  suspend fun close()

  data class AndroidConnection(
    private val dadb: Dadb,
    private val forwarder: AutoCloseable,
    private val socketJob: Job,
  ) : ActiveConnection {
    override suspend fun close() {
      dadb.close()
      forwarder.close()
      socketJob.cancel()
    }
  }

  sealed interface IosConnection : ActiveConnection {
    data class Simulator(
      private val socketJob: Job,
    ) : IosConnection {
      override suspend fun close() {
        socketJob.cancel()
      }
    }

    data class Physical(
      private val socketJob: Job,
    ) : IosConnection {
      override suspend fun close() {
        socketJob.cancel()
        IosDeviceManager.deactivate()
      }
    }
  }
}
