package com.livewire.runtime.discoverymanager

enum class DiscoverySource {
  Adb,
  Ios,
  Local,
}

sealed interface DiscoveryError {
  val source: DiscoverySource
  val message: String

  data class PortInUse(
    override val source: DiscoverySource,
    val port: Int,
  ) : DiscoveryError {
    override val message = "One or more ports required by Livewire is in use by another app. Quit the other app and restart Livewire."
  }

  data class ScanFailed(
    override val source: DiscoverySource,
    val cause: String,
  ) : DiscoveryError {
    override val message = "Couldn't scan for ${source.lostTargets()}: $cause"
  }
}

private fun DiscoverySource.lostTargets(): String = when (this) {
  DiscoverySource.Adb -> "Android devices"
  DiscoverySource.Ios -> "iOS devices"
  DiscoverySource.Local -> "desktop and iOS simulator apps"
}
