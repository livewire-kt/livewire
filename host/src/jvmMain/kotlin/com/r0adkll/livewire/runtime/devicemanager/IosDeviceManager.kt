package com.r0adkll.livewire.runtime.devicemanager

import com.r0adkll.livewire.runtime.iosbridge.IosDeviceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class IosDevice(
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
