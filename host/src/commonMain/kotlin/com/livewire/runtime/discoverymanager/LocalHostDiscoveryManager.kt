@file:OptIn(ExperimentalAtomicApi::class)

package com.livewire.runtime.discoverymanager

import com.livewire.LivewireConstants
import com.livewire.discovery.DiscoveryPacket
import com.livewire.logDebug
import com.livewire.logError
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.server.application.install
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.io.readByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

object LocalHostDiscoveryManager : PlatformDiscoveryManager {

  private val started = AtomicBoolean(false)

  final override val devices: Flow<List<HostApp>>
    field = MutableStateFlow<List<HostApp>>(emptyList())

  final override val isReady: StateFlow<Boolean>
    field = MutableStateFlow(true)

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val timeSource = TimeSource.Monotonic
  private val lastSeen = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()
  private val knownApps = mutableMapOf<String, HostApp>()
  private val lock = Any()

  override suspend fun ensureStarted() {
    if (!started.compareAndSet(expectedValue = false, newValue = true)) return

    scope.launch {
      val selectorManager = SelectorManager(Dispatchers.IO)
      try {
        aSocket(selectorManager)
          .udp()
          .bind(InetSocketAddress("0.0.0.0", LivewireConstants.UdpDiscoveryPort))
          .use { socket ->
            while (isActive) {
              try {
                val bytes = socket.receive().packet.readByteArray()
                // TODO: should this just be a continue?
                val packet = DiscoveryPacket.decode(bytes) ?: continue
                registerApp(packet.toHostApp())
              } catch (e: CancellationException) {
                throw e
              } catch (t: Throwable) {
                logError("error receiving packet: ${t.message}", t)
              }
            }
          }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logError("failed to start listener", e)
      } finally {
        selectorManager.close()
      }
    }

    // WS announce listener for clients that can't open raw sockets (browsers). Sits on the
    // UDP discovery port's TCP twin so no new port is claimed.
    scope.launch {
      try {
        val announceServer = embeddedServer(CIO, port = LivewireConstants.UdpDiscoveryPort, host = "127.0.0.1") {
          install(WebSockets)

          routing {
            webSocket(LivewireConstants.AnnouncePath) {
              var announcedId: String? = null
              try {
                for (frame in incoming) {
                  val bytes = (frame as? Frame.Binary)?.readBytes() ?: continue
                  val packet = DiscoveryPacket.decode(bytes) ?: continue
                  val app = packet.toHostApp()
                  announcedId = app.id
                  registerApp(app)
                }
              } finally {
                announcedId?.let(::removeApp)
              }
            }
          }
        }
        announceServer.start(wait = true)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logError("failed to start announce listener", e)
      }
    }

    scope.launch {
      while (isActive) {
        delay(PruneInterval)
        synchronized(lock) {
          val performedPrune = lastSeen
            .filter { (_, mark) -> mark.elapsedNow() > StaleDeviceThreshold }
            .onEach { (id, _) ->
              lastSeen.remove(id)
              knownApps.remove(id)
            }
            .isNotEmpty()

          if (performedPrune) {
            devices.value = knownApps.values.toList()
          }
        }
      }
    }
  }

  private fun registerApp(app: HostApp) {
    synchronized(lock) {
      val isNew = app.id !in knownApps
      lastSeen[app.id] = timeSource.markNow()
      knownApps[app.id] = app
      devices.value = knownApps.values.toList()
      if (isNew) {
        logDebug("discovered ${app.displayName} (${app.id})")
      }
    }
  }

  private fun removeApp(id: String) {
    synchronized(lock) {
      lastSeen.remove(id)
      if (knownApps.remove(id) != null) {
        devices.value = knownApps.values.toList()
        logDebug("removed $id")
      }
    }
  }

  override fun shutdown() {
    synchronized(lock) {
      lastSeen.clear()
      knownApps.clear()
      devices.value = emptyList()
    }

    scope.cancel()
  }

  private fun logDebug(message: String) {
    logDebug("AppDiscoveryManager", message)
  }

  private fun logError(message: String, throwable: Throwable) {
    logError("AppDiscoveryManager", message, throwable)
  }
}

private fun DiscoveryPacket.toHostApp(): HostApp = when (platform) {
  Desktop -> DesktopApp(
    instanceId = instanceId,
    appName = appName,
    processId = processId,
    appIcon = appIcon,
    protocolVersion = protocolVersion,
  )
  IosSimulator -> {
    IosApp(
      instanceId = instanceId,
      appName = appName,
      bundleId = packageName,
      appIcon = appIcon,
      device = IosDevice(
        connection = IosDeviceConnection.forSimulator(),
        udid = instanceId,
        name = deviceName,
        deviceType = Simulator,
        osVersion = osVersion,
      ),
      protocolVersion = protocolVersion,
    )
  }
  Web -> WebApp(
    instanceId = instanceId,
    appName = appName,
    pageOrigin = packageName,
    browser = deviceName,
    appIcon = appIcon,
    protocolVersion = protocolVersion,
  )
  else -> error("Unexpected platform: $platform")
}

private const val PruneInterval = 2000L
private val StaleDeviceThreshold = 5000.milliseconds
