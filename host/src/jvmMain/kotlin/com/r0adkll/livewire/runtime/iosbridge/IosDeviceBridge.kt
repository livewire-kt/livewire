package com.r0adkll.livewire.runtime.iosbridge

import com.r0adkll.livewire.LivewireConstants
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.runtime.devicemanager.IosDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

class IosDeviceBridge(private val scope: CoroutineScope) {
  private val started = AtomicBoolean(false)
  private val stateLock = Mutex()

  val devices: StateFlow<List<IosDevice>>
    field = MutableStateFlow<List<IosDevice>>(emptyList())

  private val physicalByUdid = mutableMapOf<String, PhysicalDevice>()
  private val physicalInfoByUdid = mutableMapOf<String, IosDevice>()
  private var simulatorDevices: List<IosDevice> = emptyList()

  private var usbmuxJob: Job? = null
  private var simulatorJob: Job? = null
  private var forwarder: IosForwarder? = null

  fun start() {
    if (!started.compareAndSet(false, true)) return

    usbmuxJob = scope.launch { runUsbMuxLoop() }
    simulatorJob = scope.launch { runSimulatorLoop() }
  }

  fun shutdown() {
    forwarder?.stop()
    forwarder = null

    usbmuxJob?.cancel()
    usbmuxJob = null

    simulatorJob?.cancel()
    simulatorJob = null

    started.set(false)
  }

  suspend fun activate(udid: String): Boolean {
    val physical = stateLock.withLock { physicalByUdid[udid] } ?: return false

    forwarder?.stop()
    forwarder = IosForwarder(
      deviceId = physical.deviceId,
      forwardPort = LivewireConstants.Port,
      bridgePort = LivewireConstants.BridgePort,
    ).also { it.start() }

    return true
  }

  fun deactivate(): Boolean {
    forwarder?.stop()
    forwarder = null
    return true
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

      val initialEvents = runCatching { client.listen() }.getOrNull()

      if (initialEvents == null) {
        logDebug("failed to listen for usbmuxd events")
        client.close()
        delay(500)
        continue
      }

      logDebug("received ${initialEvents.size} initial usbmuxd events")
      for (event in initialEvents) {
        handleUsbMuxEvent(event)
      }

      while (scope.isActive) {
        val event = client.nextEvent() ?: break
        handleUsbMuxEvent(event)
      }

      client.close()
      delay(500)
    }
  }

  private suspend fun runSimulatorLoop() {
    while (scope.isActive) {
      val simulators = querySimulators()
      stateLock.withLock {
        simulatorDevices = simulators
        updateDeviceList()
      }
      delay(2000)
    }
  }

  private suspend fun handleUsbMuxEvent(event: UsbMuxEvent) {
    when (event) {
      is UsbMuxEvent.Attach -> {
        logDebug("device attached deviceId=${event.deviceId} udid=${event.udid}")
        val udid = event.udid ?: return
        val device = PhysicalDevice(deviceId = event.deviceId, udid = udid)
        val info = loadPhysicalInfo(udid)
        stateLock.withLock {
          physicalByUdid[udid] = device
          physicalInfoByUdid[udid] = info
          updateDeviceList()
        }
      }

      is UsbMuxEvent.Detach -> {
        logDebug("device detached deviceId=${event.deviceId} udid=${event.udid}")
        stateLock.withLock {
          if (event.udid != null) {
            physicalByUdid.remove(event.udid)
            physicalInfoByUdid.remove(event.udid)
          } else {
            val existing = physicalByUdid.entries.firstOrNull { it.value.deviceId == event.deviceId }
            if (existing != null) {
              physicalByUdid.remove(existing.key)
              physicalInfoByUdid.remove(existing.key)
            }
          }
          updateDeviceList()
        }
      }
    }
  }

  private fun updateDeviceList() {
    devices.value = physicalInfoByUdid.values + simulatorDevices
  }

  private fun logDebug(message: String) {
    logDebug("ios-bridge", message)
  }
}

private fun loadPhysicalInfo(udid: String): IosDevice {
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
    udid = udid,
    name = name ?: udid,
    deviceType = Physical,
    osVersion = osVersion ?: "unknown",
  )
}

private fun querySimulators(): List<IosDevice> {
  if (!isMacOs()) return emptyList()

  val result = runCommand("xcrun", "simctl", "list", "devices", "booted")

  if (result.exitCode != 0) return emptyList()

  return buildList {
    var osVersion: String? = null

    result.stdout.lines().forEach { line ->
      if (line.startsWith("-- ") && line.endsWith(" --")) {
        osVersion = line.substringAfter("-- ").substringBefore(" --").removePrefix("iOS ")
      } else if (line.startsWith("  ") && osVersion != null) {
        val match = SimLineRegex.matchEntire(line.trim()) ?: return@forEach
        add(
          IosDevice(
            udid = match.groupValues[2],
            name = match.groupValues[1],
            deviceType = Simulator,
            osVersion = osVersion,
          )
        )
      }
    }
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

private data class PhysicalDevice(
  val deviceId: Int,
  val udid: String,
)

private fun isMacOs(): Boolean = System.getProperty("os.name").lowercase().contains("mac")

private val SimLineRegex = Regex("""^(.*?) \(([A-F0-9-]{36})\) \((.*?)\)$""")
