@file:OptIn(ExperimentalAtomicApi::class)

package com.r0adkll.livewire.runtime.devicemanager

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.discovery.DiscoveryPacket
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

data class DesktopDevice(
  val instanceId: String,
  val appName: String,
  val processId: Long,
) : HostDevice {
  override val id: String = "desktop:$instanceId"
  override val displayName: String = appName
}

object DesktopDeviceManager : PlatformDeviceManager {

  private val started = AtomicBoolean(false)

  final override val devices: Flow<List<HostDevice>>
    field = MutableStateFlow<List<HostDevice>>(emptyList())

  final override val isReady: StateFlow<Boolean>
    field = MutableStateFlow(true)

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val timeSource = TimeSource.Monotonic
  private val lastSeen = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()
  private val knownDevices = mutableMapOf<String, DesktopDevice>()
  private val lock = Any()

  override suspend fun ensureStarted() {
    if (!started.compareAndSet(expectedValue = false, newValue = true)) return

    scope.launch {
      val selectorManager = SelectorManager(Dispatchers.IO)
      try {
        aSocket(selectorManager)
          .udp()
          .bind(InetSocketAddress("0.0.0.0", LivewireConstants.DiscoveryPort))
          .use { socket ->
            while (isActive) {
              try {
                val json = socket.receive().packet.readText()
                val packet = Json.decodeFromString<DiscoveryPacket>(json)

                val device = DesktopDevice(
                  instanceId = packet.instanceId,
                  appName = packet.appName,
                  processId = packet.processId,
                )

                synchronized(lock) {
                  lastSeen[packet.instanceId] = timeSource.markNow()
                  knownDevices[packet.instanceId] = device
                  devices.value = knownDevices.values.toList()
                }
              } catch (e: CancellationException) {
                throw e
              } catch (t: Throwable) {
                logError("DesktopDeviceManager", "error receiving packet: ${t.message}", t)
              }
            }
          }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logError("DesktopDeviceManager", "failed to start listener", e)
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
              knownDevices.remove(id)
            }
            .isNotEmpty()

          if (performedPrune) {
            devices.value = knownDevices.values.toList()
          }
        }
      }
    }
  }

  override fun shutdown() {
    synchronized(lock) {
      lastSeen.clear()
      knownDevices.clear()
      devices.value = emptyList()
    }

    scope.cancel()
  }
}

private const val PruneInterval = 2000L
private val StaleDeviceThreshold = 5000.milliseconds
