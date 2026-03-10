package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.runtime.HostConnectionState.Error
import com.r0adkll.livewire.runtime.devicemanager.AdbDevice
import com.r0adkll.livewire.runtime.devicemanager.HostDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceManager
import com.r0adkll.livewire.transport.PayloadDecoder
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
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

enum class HostConnectionState {
  Disconnected,
  Forwarding,
  Connecting,
  Connected,
  Error,
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

  val connectionState: StateFlow<HostConnectionState>
    field = MutableStateFlow(Disconnected)

  val incomingMessages: SharedFlow<Any>
    field = MutableSharedFlow<Any>(extraBufferCapacity = 64)

  val incomingLayoutNodes: StateFlow<LayoutNode>
    field = MutableStateFlow<LayoutNode>(RootNode())

  private var activeConnection: ActiveConnection? = null

  var session: WebSocketSession? = null
    private set

  val codec = LivewireWebSocketCodec(
    decoders = decoders.toSet()
  )

  suspend fun connect(device: HostDevice) {
    println("connect to $device")
    disconnect()

    when (device) {
      is AdbDevice -> connectAndroid(device)
      is IosDevice -> connectIos(device)
    }
  }

  private fun connectAndroid(device: AdbDevice) {
    scope.launch {
      try {
        connectionState.value = Forwarding
        activeConnection = ActiveConnection.AndroidConnection(
          dadb = device.connection,
          forwarder = device.connection.tcpForward(LivewireConstants.Port, LivewireConstants.Port),
          socketJob = startClientConnection(),
        )
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun connectIos(device: IosDevice) {
    when (device.deviceType) {
      Simulator -> connectIosSimulator()
      Physical -> connectIosPhysical(device)
    }
  }

  private fun connectIosSimulator() {
    scope.launch {
      try {
        activeConnection = ActiveConnection.IosConnection.Simulator(startClientConnection())
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun connectIosPhysical(device: IosDevice) {
    scope.launch {
      try {
        connectionState.value = Forwarding
        val ok = IosDeviceManager.activate(device.udid)
        if (!ok) {
          connectionState.value = Error
          return@launch
        }
        activeConnection = ActiveConnection.IosConnection.Physical(startClientConnection())
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun startClientConnection(): Job {
    connectionState.value = Connecting

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
          connectionState.value = Connected
          try {
            for (frame in incoming) {
              try {
                when (val incomingMessage = codec.decode(frame)) {
                  is LivewireIncoming.Payload -> incomingMessages.tryEmit(incomingMessage.payload)
                  is LivewireIncoming.Layout -> incomingLayoutNodes.emit(incomingMessage.node)
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
        connectionState.value = Error
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

    connectionState.value = Disconnected
    incomingLayoutNodes.value = RootNode()
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
