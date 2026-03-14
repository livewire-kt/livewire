package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.crypto.LivewireHandshake
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.runtime.HostConnectionState.Error
import com.r0adkll.livewire.runtime.discoverymanager.AdbDevice
import com.r0adkll.livewire.runtime.discoverymanager.AndroidApp
import com.r0adkll.livewire.runtime.discoverymanager.DesktopApp
import com.r0adkll.livewire.runtime.discoverymanager.HostApp
import com.r0adkll.livewire.runtime.discoverymanager.IosApp
import com.r0adkll.livewire.runtime.discoverymanager.IosDevice
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RootNode
import com.r0adkll.livewire.ui.transport.LivewireIncoming
import com.r0adkll.livewire.ui.transport.LivewireWebSocketCodec
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.CoroutineContext

enum class HostConnectionState {
  Disconnected,
  Forwarding,
  Listening,
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
  private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

  private var expectedAppId: String? = null

  var session: WebSocketSession? = null
    private set

  val codec = LivewireWebSocketCodec(
    decoders = decoders.toSet()
  )

  suspend fun connect(app: HostApp) {
    logDebug("connect to ${app.id}")
    disconnect()

    expectedAppId = app.instanceId

    when (app) {
      is AndroidApp -> connectAndroid(app)
      is IosApp -> connectIos(app)
      is DesktopApp -> connectDesktop()
    }
  }

  private fun connectAndroid(app: AndroidApp) {
    scope.launch {
      try {
        connectionState.value = Forwarding
        val forwarder = AdbReverseForwarder(app.device, LivewireConstants.Port).also { it.start() }
        startServer()
        activeConnection = ActiveConnection.AndroidConnection(forwarder)
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun connectIos(app: IosApp) {
    when (app.device.deviceType) {
      Simulator -> connectIosSimulator()
      Physical -> connectIosPhysical(app.device)
    }
  }

  private fun connectIosSimulator() {
    scope.launch {
      try {
        startServer()
        activeConnection = ActiveConnection.IosSimulatorConnection
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
        val ok = device.connection.activate()
        if (!ok) {
          connectionState.value = Error
          return@launch
        }
        startServer()
        activeConnection = ActiveConnection.IosPhysicalConnection(device.connection)
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun connectDesktop() {
    scope.launch {
      try {
        startServer()
        connectionState.value = Listening
        activeConnection = ActiveConnection.DesktopConnection
      } catch (e: Exception) {
        e.printStackTrace()
        connectionState.value = Error
      }
    }
  }

  private fun startServer() {
    if (server != null) {
      connectionState.value = Listening
      return
    }

    server = embeddedServer(CIO, port = LivewireConstants.Port) {
      install(WebSockets)

      routing {
        route(LivewireConstants.WsPath) {
          // prevent clients from entering the Connected state on a connection that will be immediately closed.
          intercept(ApplicationCallPipeline.Plugins) {
            if (expectedAppId != context.request.queryParameters["connection_id"]) {
              context.respond(HttpStatusCode.Forbidden)
              finish()
            }
          }

          webSocket {
            session?.close()
            session = this

            logDebug("performing encryption handshake…")
            codec.secureSession = LivewireHandshake().perform(
              sendBytes = { bytes -> send(Frame.Binary(true, bytes)) },
              receiveBytes = { (incoming.receive() as Frame.Binary).readBytes() },
            )
            logDebug("encryption handshake complete")

            connectionState.value = Connected
            logDebug("client connected (session=${this.hashCode()})")
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
              codec.secureSession = null
              if (session == this@webSocket) {
                session = null
                connectionState.value = if (server != null) Listening else Disconnected
                logDebug("client disconnected")
              }
            }
          }
        }
      }
    }.also {
      it.start(wait = false)
      connectionState.value = Listening
    }
  }

  private fun stopServer() {
    server?.stop(1000, 2000)
    server = null
  }

  suspend inline fun <reified T : Any> send(payload: T) {
    session?.send(codec.encodePayload(payload))
  }

  suspend fun disconnect() {
    expectedAppId = null

    session?.close()
    session = null

    activeConnection?.close()
    activeConnection = null

    connectionState.value = Disconnected
    incomingLayoutNodes.value = RootNode()
  }

  suspend fun close() {
    disconnect()
    stopServer()
    connectionState.value = Disconnected
    scope.cancel()
  }

  private fun logDebug(message: String) {
    logDebug("host-connection", message)
  }
}

sealed interface ActiveConnection {
  suspend fun close()

  data class AndroidConnection(
    private val forwarder: AutoCloseable,
  ) : ActiveConnection {
    override suspend fun close() {
      forwarder.close()
    }
  }

  data object IosSimulatorConnection : ActiveConnection {
    override suspend fun close() = Unit
  }

  data class IosPhysicalConnection(
    private val connection: AutoCloseable,
  ) : ActiveConnection {
    override suspend fun close() {
      connection.close()
    }
  }

  data object DesktopConnection : ActiveConnection {
    override suspend fun close() = Unit
  }
}

private class AdbReverseForwarder(
  private val device: AdbDevice,
  private val port: Int,
) : AutoCloseable {
  fun start() {
    try {
      device.connection.open("reverse:forward:tcp:$port;tcp:$port").close()
    } catch (e: IOException) {
      throw IOException("Failed to set up reverse forward", e)
    }
  }

  override fun close() {
    try {
      device.connection.open("reverse:killforward:tcp:$port").close()
    } catch (e: IOException) {
      logDebug("adb", "failed to remove reverse forward: ${e.message}")
    }
  }
}
