package com.r0adkll.livewire.runtime.discoverymanager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

object CompositeDiscoveryManager {
  suspend fun appList(): Flow<List<HostApp>> {
    DiscoveryManagers.forEach { it.ensureStarted() }

    return combine(*DiscoveryManagers.map { it.devices }.toTypedArray()) { unmergedDevices ->
      unmergedDevices.flatMap { it }.sortedBy { it.displayName }
    }
  }

  fun isReady(): Flow<Boolean> = combine(DiscoveryManagers.map { it.isReady }) { readyStates ->
    readyStates.all { it }
  }

  fun shutdown() {
    DiscoveryManagers.forEach { it.shutdown() }
  }
}

private val DiscoveryManagers = listOf(
  AdbDiscoveryManager,
  IosDiscoveryManager,
  LocalHostDiscoveryManager,
)
