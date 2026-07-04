package com.livewire.runtime.discoverymanager

import com.livewire.runtime.iosbridge.IosDeviceBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

object IosDiscoveryManager : PlatformDiscoveryManager {
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val bridge = IosDeviceBridge(scope)

  override val devices: Flow<List<IosApp>> = bridge.devices
  override val isReady: StateFlow<Boolean> = bridge.isReady

  override suspend fun ensureStarted() {
    bridge.ensureStarted()
  }

  override fun shutdown() {
    bridge.shutdown()
    scope.cancel()
  }
}

class IosDeviceConnection private constructor(
  private val physicalUdid: String?,
  private val bridge: IosDeviceBridge?,
) : AutoCloseable {
  private var forwarder: AutoCloseable? = null

  suspend fun activate(): Boolean {
    forwarder?.close()

    bridge?.ensureStarted()

    return if (physicalUdid != null && bridge != null) {
      forwarder = bridge.activate(physicalUdid)
      forwarder != null
    } else {
      true
    }
  }

  override fun close() {
    forwarder?.close()
  }

  companion object {
    internal fun forPhysical(udid: String, bridge: IosDeviceBridge) = IosDeviceConnection(
      physicalUdid = udid,
      bridge = bridge,
    )

    internal fun forSimulator() = IosDeviceConnection(
      physicalUdid = null,
      bridge = null,
    )
  }
}
