package com.livewire.runtime.iosbridge

import com.livewire.LivewireConstants
import com.livewire.discovery.DiscoveryPacket
import com.livewire.logDebug
import com.livewire.runtime.discoverymanager.IosApp
import com.livewire.runtime.discoverymanager.IosDevice
import com.livewire.runtime.discoverymanager.IosDeviceConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.nio.channels.Channels
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class IosDeviceBridge(private val scope: CoroutineScope) {
  private val started = AtomicBoolean(false)
  private val stateLock = Mutex()

  val devices: StateFlow<List<IosApp>>
    field = MutableStateFlow<List<IosApp>>(emptyList())

  val isReady: StateFlow<Boolean>
    field = MutableStateFlow(false)

  private val deviceIdMap = mutableMapOf<Udid, Int>()
  private val physicalDeviceMap = mutableMapOf<Udid, IosDevice>()
  private val discoveredApps = mutableMapOf<Udid, IosApp>()
  private val discoveryJobs = mutableMapOf<Udid, Job>()

  private var usbmuxJob: Job? = null

  private val usbmuxClient = AtomicReference<UsbMuxClient?>(null)

  fun ensureStarted() {
    if (!started.compareAndSet(false, true)) return

    usbmuxJob = scope.launch { runUsbMuxLoop() }
  }

  fun shutdown(forwarder: AutoCloseable? = null) {
    forwarder?.close()

    runCatching { usbmuxClient.getAndSet(null)?.close() }

    usbmuxJob?.cancel()
    usbmuxJob = null

    discoveryJobs.values.forEach { it.cancel() }
    discoveryJobs.clear()

    started.set(false)
  }

  suspend fun activate(udid: String): AutoCloseable? {
    val deviceId = stateLock.withLock { deviceIdMap[Udid(udid)] } ?: return null

    return IosForwarder(
      deviceId = deviceId,
      forwardPort = LivewireConstants.Port,
      bridgePort = LivewireConstants.BridgePort,
    ).also { it.start() }
  }

  private suspend fun runUsbMuxLoop() {
    while (scope.isActive) {
      val client = runCatching {
        UsbMuxClient.connect(Paths.get(UsbmuxdPath)).also {
          logDebug("connected to usbmuxd at $UsbmuxdPath")
        }
      }.getOrNull()

      if (client == null) {
        logDebug("failed to connect to usbmuxd at $UsbmuxdPath")
        delay(500)
        continue
      }

      usbmuxClient.set(client)

      val initialEvents = runCatching { client.listen() }.getOrNull()

      if (initialEvents == null) {
        logDebug("failed to listen for usbmuxd events")
        usbmuxClient.compareAndSet(client, null)
        client.close()
        delay(500)
        continue
      }

      logDebug("received ${initialEvents.size} initial usbmuxd events")
      for (event in initialEvents) {
        handleUsbMuxEvent(event)
      }

      if (!isReady.value) {
        isReady.value = true
      }

      while (scope.isActive) {
        val event = client.nextEvent() ?: break
        handleUsbMuxEvent(event)
      }

      usbmuxClient.compareAndSet(client, null)
      client.close()
      delay(500)
    }
  }

  private suspend fun handleUsbMuxEvent(event: UsbMuxEvent) {
    when (event) {
      is UsbMuxEvent.Attach -> {
        logDebug("device attached: deviceId=${event.deviceId} udid=${event.udid}")
        val udid = event.udid?.let { Udid(it) } ?: return

        stateLock.withLock {
          deviceIdMap[udid] = event.deviceId
          physicalDeviceMap[udid] = loadPhysicalDevice(udid.value)
        }

        discoveryJobs[udid]?.cancel()
        discoveryJobs[udid] = scope.launch {
          runPhysicalDeviceDiscovery(event.deviceId, udid)
        }
      }

      is UsbMuxEvent.Detach -> {
        logDebug("device detached: deviceId=${event.deviceId} udid=${event.udid}")
        val udid = event.udid?.let { Udid(it) } ?: run {
          stateLock.withLock {
            deviceIdMap.entries.firstOrNull { it.value == event.deviceId }?.key
          }
        } ?: return

        discoveryJobs[udid]?.cancel()
        discoveryJobs.remove(udid)

        stateLock.withLock {
          deviceIdMap.remove(udid)
          physicalDeviceMap.remove(udid)
          discoveredApps.remove(udid)
          updateDeviceList()
        }
      }
    }
  }

  private suspend fun runPhysicalDeviceDiscovery(deviceId: Int, udid: Udid) {
    while (scope.isActive) {
      val packet = tryTcpDiscoveryRange(deviceId)

      stateLock.withLock {
        if (packet != null) {
          val device = physicalDeviceMap[udid]
          if (device != null) {
            discoveredApps[udid] = IosApp(
              instanceId = packet.instanceId,
              appName = packet.appName,
              bundleId = packet.packageName,
              device = device,
              appIcon = packet.appIcon,
              protocolVersion = packet.protocolVersion,
            )
            updateDeviceList()
          }
        } else {
          if (discoveredApps.remove(udid) != null) {
            updateDeviceList()
          }
        }
      }

      delay(RefreshRateMs)
    }
  }

  private fun tryTcpDiscoveryRange(deviceId: Int): DiscoveryPacket? {
    for (port in LivewireConstants.TcpDiscoveryPorts) {
      val packet = tryTcpDiscovery(deviceId, port)
      if (packet != null) return packet
    }
    return null
  }

  private fun tryTcpDiscovery(deviceId: Int, port: Int): DiscoveryPacket? {
    val client = runCatching {
      UsbMuxClient.connect(Paths.get(UsbmuxdPath))
    }.getOrNull() ?: return null

    val stream = runCatching {
      client.connectToDevice(deviceId, port)
    }.getOrNull()

    if (stream == null) {
      client.close()
      return null
    }

    return try {
      Channels.newInputStream(stream).bufferedReader().readLine()?.let { Json.decodeFromString<DiscoveryPacket>(it) }
    } catch (e: Exception) {
      logDebug("tcp discovery failed for deviceId=$deviceId: ${e.message}")
      null
    } finally {
      runCatching { stream.close() }
      runCatching { client.close() }
    }
  }

  private fun updateDeviceList() {
    devices.value = discoveredApps.values.toList()
  }

  private fun logDebug(message: String) {
    logDebug("ios-bridge", message)
  }

  private fun loadPhysicalDevice(udid: String): IosDevice {
    var name: String? = null
    var osVersion: String? = null

    // Try ideviceinfo (mostly linux, but could be macos too) first
    val idevice = runCommand("ideviceinfo", "-u", udid)
    if (idevice.exitCode == 0) {
      idevice.stdout.lineSequence().forEach { line ->
        val parts = line.split(": ", limit = 2)
        if (parts.size != 2) return@forEach
        when (parts[0].trim()) {
          "DeviceName" -> name = parts[1].trim()
          "ProductVersion" -> osVersion = parts[1].trim()
        }
      }
    }

    // Fall back to xcrun if ideviceinfo isn't installed
    if ((name == null || osVersion == null) && isMacOs()) {
      val result = runCommand(
        "xcrun",
        "devicectl",
        "list",
        "devices",
        "--filter",
        "udid == '$udid'",
        "--hide-default-columns",
        "--columns",
        "name",
        "osBuild",
        "--hide-headers"
      )

      val resultLines = result.stdout.lines()
      if (result.exitCode == 0 && resultLines.isNotEmpty()) {
        val line = resultLines[0].trim()
        val lastSpaceIndex = line.lastIndexOf("  ")
        if (lastSpaceIndex != -1) {
          name = line.substring(0, lastSpaceIndex).trim()
          osVersion = line.substring(lastSpaceIndex).trim()
        }
      }
    }

    return IosDevice(
      connection = IosDeviceConnection.forPhysical(udid, this),
      udid = udid,
      name = name ?: udid,
      deviceType = Physical,
      osVersion = osVersion ?: "unknown",
    )
  }
}

private fun runCommand(vararg args: String): CommandResult {
  return try {
    val process = ProcessBuilder(*args)
      .redirectErrorStream(false)
      .start()
    val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
    val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)
    val exitCode = process.waitFor()
    CommandResult(exitCode, stdout, stderr)
  } catch (e: Exception) {
    CommandResult(1, "", e.message ?: "")
  }
}

private data class CommandResult(
  val exitCode: Int,
  val stdout: String,
  val stderr: String,
)

@JvmInline
value class Udid(val value: String)

private fun isMacOs(): Boolean = System.getProperty("os.name").lowercase().contains("mac")

private const val RefreshRateMs = 2000L
