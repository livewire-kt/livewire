@file:OptIn(ExperimentalAtomicApi::class)

package com.r0adkll.livewire.runtime.discoverymanager

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.discovery.DiscoveryPacket
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.logError
import dadb.Dadb
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.readLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Discovers Android apps by scanning the TCP discovery port range via ADB forward.
 * Each running Livewire-enabled app starts a TCP discovery server on one of the
 * ports in [LivewireConstants.TcpDiscoveryPorts].
 */
object AdbDiscoveryManager : PlatformDiscoveryManager {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val started = AtomicBoolean(false)

  final override val devices: Flow<List<HostApp>>
    field = MutableStateFlow<List<HostApp>>(emptyList())

  final override val isReady: StateFlow<Boolean>
    field = MutableStateFlow(false)

  private val timeSource = TimeSource.Monotonic
  private val lastSeen = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()
  private val knownApps = mutableMapOf<String, AndroidApp>()
  private val lock = Any()

  override suspend fun ensureStarted() {
    if (started.compareAndSet(expectedValue = false, newValue = true)) {
      scope.launch {
        while (true) {
          val result = runCatching { discoverApps() }

          result.getOrNull()?.let { discoveredApps ->
            synchronized(lock) {
              val now = timeSource.markNow()
              for (app in discoveredApps) {
                val isNew = app.id !in knownApps
                lastSeen[app.id] = now
                knownApps[app.id] = app
                if (isNew) {
                  logDebug("AdbDeviceManager", "discovered ${app.displayName} (${app.id})")
                }
              }
              devices.value = knownApps.values.toList()
            }
          }
          isReady.value = true
          delay(RefreshRateMs)
        }
      }

      // Prune stale entries
      scope.launch {
        while (true) {
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
  }

  private fun discoverApps(): List<AndroidApp> {
    return Dadb.list().flatMap { dadb ->
      try {
        val device = AdbDevice(
          connection = dadb,
          serial = dadb.shell("getprop ro.serialno").output.trim().ifEmpty { "unknown" },
          model = dadb.shell("getprop ro.product.model").output.trim(),
          apiVersion = dadb.shell("getprop ro.build.version.sdk").output.trim(),
        )
        findLivewireApps(device)
      } catch (e: Exception) {
        logError("AdbDeviceManager", "error querying device: ${e.message}", e)
        emptyList()
      } finally {
        dadb.close()
      }
    }
  }

  /**
   * Scans the TCP discovery port range by setting up ADB forward for each port,
   * attempting to connect and read a [DiscoveryPacket], then cleaning up the forward.
   */
  private fun findLivewireApps(device: AdbDevice): List<AndroidApp> {
    val apps = mutableListOf<AndroidApp>()

    for (port in LivewireConstants.TcpDiscoveryPorts) {
      val localPort = port + 10000  // use offset local ports to avoid conflicts
      try {
        // Set up ADB forward: local -> device
        device.connection.open("host:forward:tcp:$localPort;tcp:$port").close()

        val packet = tryTcpDiscovery(localPort)
        if (packet != null) {
          apps += AndroidApp(
            instanceId = packet.instanceId,
            packageName = packet.packageName,
            label = packet.appName,
            device = device,
          )
        }
      } catch (e: Exception) {
        logDebug("AdbDeviceManager", "port $port scan failed on ${device.serial}: ${e.message}")
      } finally {
        // Clean up the forward
        runCatching {
          device.connection.open("host:killforward:tcp:$localPort").close()
        }
      }
    }

    return apps
  }

  private fun tryTcpDiscovery(localPort: Int): DiscoveryPacket? {
    val selectorManager = SelectorManager(Dispatchers.IO)
    return try {
      val socket = kotlinx.coroutines.runBlocking {
        withTimeoutOrNull(1000) {
          aSocket(selectorManager).tcp().connect("127.0.0.1", localPort)
        }
      } ?: return null

      val json = kotlinx.coroutines.runBlocking {
        withTimeoutOrNull(1000) {
          socket.openReadChannel().readLine()
        }
      }

      socket.close()

      if (json != null) {
        Json.decodeFromString<DiscoveryPacket>(json)
      } else null
    } catch (e: Exception) {
      null
    } finally {
      selectorManager.close()
    }
  }

  override fun shutdown() {
    scope.cancel()
    synchronized(lock) {
      lastSeen.clear()
      knownApps.clear()
      devices.value = emptyList()
    }
  }
}

private const val RefreshRateMs = 2000L
private const val PruneInterval = 2000L
private val StaleDeviceThreshold = 5000.milliseconds
