@file:OptIn(ExperimentalAtomicApi::class)

package com.livewire.runtime.discoverymanager

import com.livewire.LivewireConstants
import com.livewire.discovery.DiscoveryPacket
import com.livewire.logDebug
import com.livewire.logError
import dadb.adbserver.AdbServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

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

  private fun discoverApps(): List<AndroidApp> {
    val serials = AdbServer.listDadbs().map { it.toString() }

    (deviceCache.keys - serials.toSet()).forEach { serial ->
      deviceCache.remove(serial)?.connection?.close()
    }

    return serials.flatMap { serial ->
      try {
        val device = deviceCache.getOrPut(serial) { createDevice(serial) }
        findLivewireApps(device)
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

  private fun findLivewireApps(device: AdbDevice): List<AndroidApp> {
    return LivewireConstants.TcpDiscoveryPorts.mapNotNull { port ->
      tryTcpDiscovery(device, port)?.let { packet ->
        AndroidApp(
          instanceId = packet.instanceId,
          packageName = packet.packageName,
          label = packet.appName,
          device = device,
          protocolVersion = packet.protocolVersion,
        )
      }
    }
  }

  private fun tryTcpDiscovery(device: AdbDevice, port: Int): DiscoveryPacket? {
    return try {
      device.connection.open("tcp:$port").use { stream ->
        stream.source.readUtf8Line()?.let { Json.decodeFromString<DiscoveryPacket>(it) }
      }
    } catch (_: Exception) {
      null
    }
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
private val StaleDeviceThreshold = 5000.milliseconds
