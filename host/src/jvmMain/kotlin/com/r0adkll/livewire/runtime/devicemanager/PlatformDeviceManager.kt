package com.r0adkll.livewire.runtime.devicemanager

import kotlinx.coroutines.flow.Flow

sealed interface HostDevice {
  val id: String
  val displayName: String
}

sealed interface PlatformDeviceManager {
  val devices: Flow<List<HostDevice>>

  suspend fun ensureStarted()

  fun shutdown()
}
