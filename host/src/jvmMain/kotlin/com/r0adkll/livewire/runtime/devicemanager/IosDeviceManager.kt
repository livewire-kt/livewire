package com.r0adkll.livewire.runtime.devicemanager

import com.r0adkll.livewire.runtime.iosbridge.IosDeviceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class IosDevice(
  val connection: IosDeviceConnection,
  val udid: String,
  val name: String,
  val deviceType: IosDeviceType,
  val osVersion: String,
) : HostDevice {
  override val id: String = "ios:$udid"
  override val displayName: String = name
}

enum class IosDeviceType {
  Physical,
  Simulator,
}

object IosDeviceManager : PlatformDeviceManager {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val bridge = IosDeviceBridge(scope)

  override val devices: Flow<List<IosDevice>> = bridge.devices
  override val isReady: StateFlow<Boolean> = bridge.isReady

  override suspend fun ensureStarted() {
    bridge.ensureStarted()
  }

  override fun shutdown() {
    bridge.shutdown()
    scope.cancel()
  }
}

class IosDeviceConnection internal constructor(private val physicalUdid: String?, private val bridge: IosDeviceBridge) : AutoCloseable {
  private var forwarder: AutoCloseable? = null

  suspend fun activate(): Boolean {
    forwarder?.close()

    bridge.ensureStarted()

    return if (physicalUdid != null) {
      forwarder = bridge.activate(physicalUdid)
      forwarder != null
    } else {
      true
    }
  }

  override fun close() {
    forwarder?.close()
  }
}
