package com.r0adkll.livewire.runtime.devicemanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

object CompositeDeviceManager {
  suspend fun deviceList(): Flow<List<HostDevice>> {
    DeviceManagers.forEach { it.ensureStarted() }

    return combine(*DeviceManagers.map { it.devices }.toTypedArray()) { unmergedDevices ->
      unmergedDevices.flatMap { it }.sortedBy { it.displayName }
    }
  }

  fun shutdown() {
    DeviceManagers.forEach { it.shutdown() }
  }
}

private val DeviceManagers = listOf(
  AdbDeviceManager,
  IosDeviceManager,
)
