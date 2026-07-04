package com.livewire.client

import com.livewire.discovery.DiscoveryPacket
import java.awt.Frame

actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  val appName = System.getProperty("sun.java.command").orEmpty().substringBefore(" ").ifEmpty {
    Frame.getFrames()
      .asSequence()
      .filter { it.isVisible }.firstNotNullOfOrNull { it.title.takeIf(String::isNotBlank) }
      ?: UnknownConfigField
  }

  return DiscoveryConfig(
    packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = appName,
      packageName = System.getProperty("java.class.path") ?: UnknownConfigField,
      processId = ProcessHandle.current().pid(),
      platform = Desktop,
      deviceName = "Desktop",
      osVersion = System.getProperty("os.version") ?: UnknownConfigField,
    ),
    transport = Udp,
  )
}
