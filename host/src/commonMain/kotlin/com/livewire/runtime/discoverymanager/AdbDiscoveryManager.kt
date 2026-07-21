@file:OptIn(ExperimentalAtomicApi::class)

package com.livewire.runtime.discoverymanager

import com.livewire.LivewireConstants
import com.livewire.discovery.DiscoveryPacket
import com.livewire.logDebug
import com.livewire.logError
import dadb.adbserver.AdbServer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

object AdbDiscoveryManager : PlatformDiscoveryManager {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val started = AtomicBoolean(false)

  override val devices: Flow<List<HostApp>>
    field = MutableStateFlow<List<HostApp>>(emptyList())

  override val isReady: StateFlow<Boolean>
    field = MutableStateFlow(false)

  private val timeSource = TimeSource.Monotonic
  private val lastSeen = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()
  private val knownApps = mutableMapOf<String, AndroidApp>()
  private val lock = Any()

  private val deviceCache = mutableMapOf<String, AdbDevice>()

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

  private suspend fun discoverApps(): List<AndroidApp> {
    val serials = AdbServer.listDadbs().map { it.toString() }

    (deviceCache.keys - serials.toSet()).forEach { serial ->
      deviceCache.remove(serial)?.connection?.close()
    }

    return serials.flatMap { serial ->
      try {
        val device = deviceCache.getOrPut(serial) { createDevice(serial) }
        findLivewireApps(serial, device)
      } catch (e: Exception) {
        logError("AdbDeviceManager", "error querying device $serial: ${e.message}", e)
        deviceCache.remove(serial)?.connection?.close()
        emptyList()
      }
    }
  }

  private fun createDevice(serial: String): AdbDevice {
    val connection = AdbServer.createDadb(
      deviceQuery = "host:transport:$serial",
      connectTimeout = SocketTimeoutMs,
      socketTimeout = SocketTimeoutMs,
    )
    return AdbDevice(
      connection = connection,
      serial = connection.shell("getprop ro.serialno").output.trim().ifEmpty { serial },
      model = connection.shell("getprop ro.product.model").output.trim(),
      apiVersion = connection.shell("getprop ro.build.version.sdk").output.trim(),
    )
  }

  private suspend fun findLivewireApps(serial: String, device: AdbDevice): List<AndroidApp> = coroutineScope {
    LivewireConstants.TcpDiscoveryPorts.map { port ->
      async(Dispatchers.IO) {
        tryTcpDiscovery(serial, port)?.let { packet ->
          AndroidApp(
            instanceId = packet.instanceId,
            packageName = packet.packageName,
            label = packet.appName,
            device = device,
            appIcon = packet.appIcon,
            protocolVersion = packet.protocolVersion,
          )
        }
      }
    }
      .awaitAll()
      .filterNotNull()
  }

  private suspend fun tryTcpDiscovery(serial: String, port: Int): DiscoveryPacket? = withTimeoutOrNull(ProbeTimeoutMs) {
    runCatching {
      AdbServer.createDadb(
        deviceQuery = "host:transport:$serial",
        connectTimeout = ProbeTimeoutMs.toInt(),
        socketTimeout = ProbeTimeoutMs.toInt(),
      ).use { probe ->
        probe.open("tcp:$port").use { stream ->
          DiscoveryPacket.decode(stream.source.readByteArray())
        }
      }
    }.getOrNull()
  }

  override fun shutdown() {
    scope.cancel()
    synchronized(lock) {
      lastSeen.clear()
      knownApps.clear()
      deviceCache.values.forEach { it.connection.close() }
      deviceCache.clear()
      devices.value = emptyList()
    }
  }
}

private const val RefreshRateMs = 2000L
private const val PruneInterval = 2000L
private const val SocketTimeoutMs = 1000
private const val ProbeTimeoutMs = 500L
private val StaleDeviceThreshold = 5000.milliseconds
