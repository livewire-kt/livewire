package com.livewire.runtime

import com.livewire.LivewireConstants
import com.livewire.crypto.LivewireHandshake
import com.livewire.logDebug
import com.livewire.runtime.HostConnectionState.Error
import com.livewire.runtime.discoverymanager.AdbDevice
import com.livewire.runtime.discoverymanager.AndroidApp
import com.livewire.runtime.discoverymanager.DesktopApp
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.runtime.discoverymanager.IosApp
import com.livewire.transport.PayloadDecoder
import com.livewire.ui.data.ClientManifest
import com.livewire.ui.data.RequestFullTree
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.LayoutNodePatch
import com.livewire.ui.layout.RootNode
import com.livewire.ui.transport.LivewireIncoming
import com.livewire.ui.transport.LivewireWebSocketCodec
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
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

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

  val clientManifest: StateFlow<ClientManifest?>
    field = MutableStateFlow<ClientManifest?>(null)

  val incomingLayoutNodes: StateFlow<LayoutNode>
    field = MutableStateFlow<LayoutNode>(RootNode())

  /** Size, in bytes, of the last layout frame (full tree or patches) received from the client. */
  val incomingLayoutSize: StateFlow<Long>
    field = MutableStateFlow(0L)

  /** Total bytes of layout frames received from the client since the last connect. */
  val incomingLayoutBytesTotal: StateFlow<Long>
    field = MutableStateFlow(0L)

  private val nodeMap = mutableMapOf<Long, LayoutNode>()
  private val parentMap = mutableMapOf<Long, LayoutNode>()
  private var currentRoot: LayoutNode = RootNode()
  @Volatile
  private var awaitingResync = false

  private var activeConnection: ActiveConnection? = null
  private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

  @Volatile
  private var expectedAppId: String? = null

  private val connectMutex = Mutex()
  private var connectJob: Job? = null

  private val sessionMutex = Mutex()
  @Volatile
  private var activeHandlerJob: Job? = null

  @Volatile
  var session: WebSocketSession? = null
    private set

  val codec = LivewireWebSocketCodec(
    decoders = decoders.toSet()
  )

  suspend fun connect(app: HostApp): Unit = connectMutex.withLock {
    if (app.instanceId == expectedAppId && connectionState.value.isLive()) {
      logDebug("already targeting ${app.id} (${connectionState.value}); ignoring connect")
      return@withLock
    }

    logDebug("connect to ${app.id}")

    connectJob?.cancelAndJoin()
    teardown()

    expectedAppId = app.instanceId
    connectJob = scope.launch { runConnect(app) }
  }

  private suspend fun runConnect(app: HostApp) {
    try {
      when (app) {
        is AndroidApp -> {
          connectionState.value = Forwarding
          val forwarder = AdbReverseForwarder(app.device, LivewireConstants.Port)
          forwarder.start()
          activeConnection = ActiveConnection.AndroidConnection(forwarder)
          startServer()
        }
        is IosApp -> when (app.device.deviceType) {
          Simulator -> {
            activeConnection = ActiveConnection.IosSimulatorConnection
            startServer()
          }
          Physical -> {
            connectionState.value = Forwarding
            val ok = app.device.connection.activate()
            if (!ok) {
              connectionState.value = Error
              return
            }
            activeConnection = ActiveConnection.IosPhysicalConnection(app.device.connection)
            startServer()
          }
        }
        is DesktopApp -> {
          activeConnection = ActiveConnection.DesktopConnection
          startServer()
          connectionState.value = Listening
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      e.printStackTrace()
      connectionState.value = Error
    }
  }

  private fun HostConnectionState.isLive(): Boolean =
    this == Forwarding || this == Listening || this == Connected

  private suspend fun startServer() {
    if (server != null) {
      connectionState.value = Listening
      return
    }

    val server = embeddedServer(CIO, port = LivewireConstants.Port, host = "127.0.0.1") {
      install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
      }

      routing {
        route(LivewireConstants.WsPath) {
          intercept(ApplicationCallPipeline.Plugins) {
            if (expectedAppId != context.request.queryParameters["connection_id"]) {
              context.respond(HttpStatusCode.Forbidden)
              finish()
            }
          }

          webSocket {
            activeHandlerJob?.cancel()
            session?.close()

            sessionMutex.withLock {
              activeHandlerJob = coroutineContext[Job]
              awaitingResync = false

              val pendingLayout = Channel<ByteArray>(capacity = PendingLayoutCapacity)
              var decoderJob: Job? = null
              try {
                logDebug("performing encryption handshake…")
                codec.secureSession = LivewireHandshake().perform(
                  sendBytes = { bytes -> send(Frame.Binary(true, bytes)) },
                  receiveBytes = { (incoming.receive() as Frame.Binary).readBytes() },
                )
                logDebug("encryption handshake complete")

                if (expectedAppId == null || expectedAppId != call.request.queryParameters["connection_id"]) {
                  logDebug("connection target changed during handshake; closing")
                  return@withLock
                }

                session = this@webSocket
                connectionState.value = Connected
                logDebug("client connected (session=${this.hashCode()})")

                decoderJob = launch(Dispatchers.Default) {
                  for (bytes in pendingLayout) {
                    try {
                      when (val incoming = codec.decodeLayoutBytes(bytes)) {
                        is LivewireIncoming.Layout -> receiveFullTree(incoming.node)
                        is LivewireIncoming.Patches -> if (!awaitingResync && !receivePatches(incoming.patches)) requestResync()
                        else -> Unit
                      }
                    } catch (e: CancellationException) {
                      throw e
                    } catch (e: Throwable) {
                      logDebug("failed to decode layout: ${e.message}")
                      requestResync()
                    }
                  }
                }

                for (frame in incoming) {
                  try {
                    when (val plaintextFrame = codec.decryptFrame(frame) ?: continue) {
                      is Frame.Text -> {
                        val message = codec.decodeTextPayload(plaintextFrame)
                        if (message != null) {
                          val payload = message.payload
                          if (payload is ClientManifest) {
                            codec.serializationStrategy = payload.layoutNodeSerialization.toStrategy()
                            clientManifest.value = payload
                          }
                          incomingMessages.emit(payload)
                        }
                      }
                      is Frame.Binary -> {
                        val layoutBytes = plaintextFrame.readBytes()
                        incomingLayoutSize.value = layoutBytes.size.toLong()
                        incomingLayoutBytesTotal.value += layoutBytes.size
                        if (pendingLayout.trySend(layoutBytes).isFailure) {
                          logDebug("layout queue overflow, requesting resync")
                          requestResync()
                        }
                      }
                      else -> Unit
                    }
                  } catch (e: CancellationException) {
                    throw e
                  } catch (e: Throwable) {
                    logDebug("failed to process frame: ${e.message}")
                    e.printStackTrace()
                  }
                }
              } finally {
                pendingLayout.cancel()
                decoderJob?.let { job -> withContext(NonCancellable) { job.join() } }
                if (session == this@webSocket) {
                  codec.secureSession = null
                  session = null
                  clientManifest.value = null
                  connectionState.value = Listening
                  incomingLayoutNodes.value = RootNode()
                  logDebug("client disconnected, waiting for reconnection…")
                }
              }
            }
          }
        }
      }
    }

    this.server = server
    withContext(NonCancellable) {
      server.start(wait = false)
      server.engine.resolvedConnectors()
    }
    logDebug("server bound on port ${LivewireConstants.Port}")
    connectionState.value = Listening
  }

  private fun receiveFullTree(root: LayoutNode) {
    root.makeObservable()
    currentRoot = root
    rebuildNodeMaps(root)
    awaitingResync = false
    incomingLayoutNodes.value = root
  }

  private fun receivePatches(patches: List<LayoutNodePatch>): Boolean {
    if (patches.isEmpty()) return true
    var desynced = false
    Snapshot.withMutableSnapshot {
      patches.forEach { patch ->
        if (!applyPatch(patch)) {
          desynced = true
          logDebug("couldn't apply patch - host is desynced: $patch")
        }
      }
    }
    return !desynced
  }

  private suspend fun requestResync() {
    if (awaitingResync) return
    awaitingResync = true
    logDebug("requesting resync from client")
    runCatching { send(RequestFullTree) }.onFailure {
      awaitingResync = false
      logDebug("failed to request resync: ${it.message}")
    }
  }

  private fun rebuildNodeMaps(root: LayoutNode) {
    nodeMap.clear()
    parentMap.clear()
    nodeMap[root.nodeId] = root
    root.children.forEach { it.registerAll(nodeMap, parentMap, root) }
  }

  private fun applyPatch(patch: LayoutNodePatch): Boolean {
    when (patch) {
      is LayoutNodePatch.InsertAt -> {
        val parent = nodeMap[patch.parentNodeId] ?: return false
        patch.node.makeObservable()
        parent.insertAt(patch.index, patch.node)
        patch.node.registerAll(nodeMap, parentMap, parent)
      }
      is LayoutNodePatch.RemoveAt -> {
        val parent = nodeMap[patch.parentNodeId] ?: return false
        for (i in patch.index until patch.index + patch.count) {
          parent.children[i].deregisterAll(nodeMap, parentMap)
        }
        parent.removeAt(patch.index, patch.count)
      }
      is LayoutNodePatch.Move -> {
        val parent = nodeMap[patch.parentNodeId] ?: return false
        parent.move(patch.from, patch.to, patch.count)
      }
      is LayoutNodePatch.Clear -> {
        val node = nodeMap[patch.nodeId] ?: return false
        node.children.forEach { it.deregisterAll(nodeMap, parentMap) }
        node.removeAll()
      }
      is LayoutNodePatch.UpdateNode -> {
        val existingNode = nodeMap[patch.nodeId] ?: return false
        val parent = parentMap[patch.nodeId] ?: return false
        val index = parent.children.indexOf(existingNode)
        if (index == -1) return false

        val node = codec.serializationStrategy.decodeFromByteArray(patch.propertyBytes)
        node.makeObservable()
        node.children.addAll(existingNode.children)
        parent.children[index] = node
        nodeMap[patch.nodeId] = node
        for (child in node.children) {
          parentMap[child.nodeId] = node
        }
      }
    }
    return true
  }

  private fun stopServer() {
    server?.stop(1000, 2000)
    server = null
  }

  suspend inline fun <reified T : Any> send(payload: T) {
    try {
      session?.send(codec.encodePayload(payload))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logDebug("host-connection", "failed to send payload: ${e.message}")
    }
  }

  suspend fun disconnect(): Unit = connectMutex.withLock {
    connectJob?.cancelAndJoin()
    connectJob = null
    teardown()
  }

  private suspend fun teardown() {
    expectedAppId = null

    session?.close()
    session = null
    clientManifest.value = null

    activeConnection?.close()
    activeConnection = null

    connectionState.value = Disconnected
    incomingLayoutSize.value = 0L
    incomingLayoutBytesTotal.value = 0L
    val emptyRoot = RootNode()
    currentRoot = emptyRoot
    nodeMap.clear()
    parentMap.clear()
    incomingLayoutNodes.value = emptyRoot
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

private const val PendingLayoutCapacity = 64

private fun LayoutNode.registerAll(
  nodeMap: MutableMap<Long, LayoutNode>,
  parentMap: MutableMap<Long, LayoutNode>,
  parent: LayoutNode,
) {
  nodeMap[nodeId] = this
  parentMap[nodeId] = parent
  children.forEach { it.registerAll(nodeMap, parentMap, this) }
}

private fun LayoutNode.deregisterAll(
  nodeMap: MutableMap<Long, LayoutNode>,
  parentMap: MutableMap<Long, LayoutNode>,
) {
  nodeMap.remove(nodeId)
  parentMap.remove(nodeId)
  children.forEach { it.deregisterAll(nodeMap, parentMap) }
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
