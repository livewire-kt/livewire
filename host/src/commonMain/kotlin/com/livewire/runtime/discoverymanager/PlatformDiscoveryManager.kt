package com.livewire.runtime.discoverymanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

sealed interface PlatformDiscoveryManager {
  val devices: Flow<List<HostApp>>
  val isReady: StateFlow<Boolean>

  val error: StateFlow<DiscoveryError?>

  suspend fun ensureStarted()

  fun shutdown()
}
