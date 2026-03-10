package com.r0adkll.livewire.runtime.devicemanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface HostDevice {
  val id: String
  val displayName: String
}

sealed interface PlatformDeviceManager {
  val devices: Flow<List<HostDevice>>
  val isReady: StateFlow<Boolean>

  suspend fun ensureStarted()

  fun shutdown()
}
