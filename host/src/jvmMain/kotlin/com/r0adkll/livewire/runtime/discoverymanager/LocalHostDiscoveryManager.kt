@file:OptIn(ExperimentalAtomicApi::class)

package com.r0adkll.livewire.runtime.discoverymanager

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.discovery.DiscoveryPacket
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.readText
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
import kotlinx.serialization.json.Json
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
                val json = socket.receive().packet.readText()
                val packet = Json.decodeFromString<DiscoveryPacket>(json)

                val app = packet.toHostApp()

                synchronized(lock) {
                  val isNew = app.id !in knownApps
                  lastSeen[app.id] = timeSource.markNow()
                  knownApps[app.id] = app
                  devices.value = knownApps.values.toList()
                  if (isNew) {
                    logDebug("discovered ${app.displayName} (${app.id})")
                  }
                }
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
  )
  IosSimulator -> {
    IosApp(
      instanceId = instanceId,
      appName = appName,
      bundleId = packageName,
      device = IosDevice(
        connection = IosDeviceConnection.forSimulator(),
        udid = instanceId,
        name = deviceName,
        deviceType = Simulator,
        osVersion = osVersion,
      ),
    )
  }
  else -> error("Unexpected platform: $platform")
}

private const val PruneInterval = 2000L
private val StaleDeviceThreshold = 5000.milliseconds
