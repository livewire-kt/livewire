package com.r0adkll.livewire.discovery

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryPacket(
  val instanceId: String,
  val appName: String,
  val processId: Long,
)
