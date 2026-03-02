package com.r0adkll.livewire.runtime.devicemanager

import com.r0adkll.livewire.runtime.iosbridge.IosDeviceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IosDeviceInfo(
  val udid: String,
  val name: String,
  @SerialName("device_type")
  val deviceType: IosDeviceType,
  @SerialName("os_version")
  val osVersion: String,
) : HostDevice {
  override val id: String = "ios:$udid"
  override val displayName: String = when (deviceType) {
    IosDeviceType.Physical -> "$name (iOS device)"
    IosDeviceType.Simulator -> "$name (iOS simulator)"
  }
}

@Serializable
enum class IosDeviceType {
  @SerialName("physical")
  Physical,

  @SerialName("simulator")
  Simulator,
}

object IosDeviceManager : PlatformDeviceManager {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val bridge = IosDeviceBridge(scope)

  override val devices: Flow<List<IosDeviceInfo>> = bridge.devices

  override suspend fun ensureStarted() {
    bridge.start()
  }

  override fun shutdown() {
    bridge.shutdown()
    scope.cancel()
  }

  suspend fun activate(udid: String): Boolean {
    ensureStarted()
    return bridge.activate(udid)
  }

  fun deactivate() {
    bridge.deactivate()
  }
}
