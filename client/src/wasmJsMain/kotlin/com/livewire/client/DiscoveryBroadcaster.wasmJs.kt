package com.livewire.client

import com.livewire.discovery.DiscoveryPacket
import com.livewire.discovery.DiscoveryPlatform.Web

private fun documentTitle(): String = js("document.title")
private fun locationHost(): String = js("window.location.host")
private fun locationOrigin(): String = js("window.location.origin")
private fun userAgent(): String = js("navigator.userAgent")

actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  return DiscoveryConfig(
    packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = documentTitle().ifEmpty { locationHost() },
      packageName = locationOrigin(),
      processId = 0L,
      platform = Web,
      deviceName = browserName(userAgent()),
      osVersion = UnknownConfigField,
      appIcon = null,
    ),
    transport = Announce,
  )
}

private fun browserName(userAgent: String): String = when {
  "Edg/" in userAgent -> "Edge"
  "OPR/" in userAgent -> "Opera"
  "Firefox/" in userAgent -> "Firefox"
  "Chrome/" in userAgent -> "Chrome"
  "Safari/" in userAgent -> "Safari"
  else -> "Browser"
}
