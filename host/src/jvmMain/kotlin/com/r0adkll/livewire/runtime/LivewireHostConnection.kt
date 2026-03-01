package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.runtime.devicemanager.AdbDevice
import com.r0adkll.livewire.runtime.devicemanager.HostDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceInfo
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceManager
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceType
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.data.LayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RootNode
import com.r0adkll.livewire.ui.transport.LivewireIncoming
import com.r0adkll.livewire.ui.transport.LivewireWebSocketCodec
import dadb.Dadb
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
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

  val codec = LivewireWebSocketCodec(decoders.toSet())

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
      engine {
        endpoint {
          connectAttempts = 1
          connectTimeout = 3000
        }
        requestTimeout = 3000
      }
      install(WebSockets)
    }

    val job = scope.launch {
      try {
        httpClient.webSocket(
          host = "127.0.0.1",
          port = LivewireConstants.Port,
          path = LivewireConstants.WsPath,
        ) {
          session = this
          _connectionState.value = HostConnectionState.CONNECTED
          try {
            for (frame in incoming) {
              try {
                when (val incomingMessage = codec.decode(frame)) {
                  is LivewireIncoming.Payload -> _incomingMessages.tryEmit(incomingMessage.payload)
                  is LivewireIncoming.Layout -> _incomingLayoutNodes.emit(incomingMessage.node)
                  null -> Unit
                }
              } catch (e: Exception) {
                logDebug("failed to decode frame: ${e.message}")
                e.printStackTrace()
              }
            }
          } finally {
            logDebug("websocket closed")
            disconnect()
          }
        }
      } catch (e: Exception) {
        logDebug("failed to connect or communicate: ${e.message}")
        e.printStackTrace()
        _connectionState.value = HostConnectionState.ERROR
      }
    }

    return job
  }

  suspend inline fun <reified T : Any> send(payload: T) {
    session?.send(codec.encodePayload(payload))
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

  private fun logDebug(message: String) {
    logDebug("host-connection", message)
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
