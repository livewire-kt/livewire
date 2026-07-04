package com.livewire.client

import com.livewire.discovery.DiscoveryPacket
import com.livewire.discovery.DiscoveryPlatform.IosPhysical
import com.livewire.discovery.DiscoveryPlatform.IosSimulator
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

@OptIn(ExperimentalForeignApi::class)
actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  val isSimulator = NSProcessInfo.processInfo.environment["SIMULATOR_UDID"] != null

  val packet = DiscoveryPacket(
    instanceId = instanceId,
    appName = NSBundle.mainBundle.infoDictionary?.get("CFBundleName") as? String ?: UnknownConfigField,
    packageName = NSBundle.mainBundle.infoDictionary?.get("CFBundleIdentifier") as? String ?: UnknownConfigField,
    processId = NSProcessInfo.processInfo.processIdentifier.toLong(),
    platform = if (isSimulator) IosSimulator else IosPhysical,
    deviceName = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] as? String ?: UIDevice.currentDevice.name,
    osVersion = UIDevice.currentDevice.systemVersion,
  )

  return DiscoveryConfig(
    packet = packet,
    transport = if (isSimulator) Udp else Tcp,
  )
}
