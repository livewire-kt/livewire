package com.livewire.discovery

import kotlinx.serialization.Serializable

@Serializable
enum class DiscoveryPlatform {
  Android,
  IosSimulator,
  IosPhysical,
  Desktop,
}

@Serializable
data class DiscoveryPacket(
  val instanceId: String,
  val appName: String,
  val packageName: String,
  val processId: Long,
  val platform: DiscoveryPlatform,
  val deviceName: String,
  val osVersion: String,
)
