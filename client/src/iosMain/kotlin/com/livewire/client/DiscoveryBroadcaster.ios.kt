package com.livewire.client

import com.livewire.discovery.DiscoveryPacket
import com.livewire.discovery.DiscoveryPlatform.IosPhysical
import com.livewire.discovery.DiscoveryPlatform.IosSimulator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSProcessInfo
import platform.posix.memcpy
import platform.UIKit.UIDevice
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

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
    appIcon = loadAppIcon(),
  )

  return DiscoveryConfig(
    packet = packet,
    transport = if (isSimulator) Udp else Tcp,
  )
}

private fun loadAppIcon(): ByteArray? = runCatching {
  val icons = NSBundle.mainBundle.infoDictionary?.get("CFBundleIcons") as? Map<*, *> ?: return null
  val primaryIcon = icons["CFBundlePrimaryIcon"] as? Map<*, *> ?: return null
  val iconFiles = primaryIcon["CFBundleIconFiles"] as? List<*> ?: return null
  val iconName = iconFiles.lastOrNull() as? String ?: return null
  val image = UIImage.imageNamed(iconName) ?: return null

  val data = UIImageJPEGRepresentation(image.scaledTo(AppIconSizePx), AppIconJpegQuality) ?: return null
  data.toByteArray()
}.getOrNull()

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
  val size = length.toInt()
  if (size == 0) return ByteArray(0)
  return ByteArray(size).apply {
    usePinned { pinned ->
      memcpy(pinned.addressOf(0), bytes, length)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.scaledTo(size: Double): UIImage {
  UIGraphicsBeginImageContextWithOptions(CGSizeMake(size, size), true, 1.0)
  return try {
    drawInRect(CGRectMake(0.0, 0.0, size, size))
    UIGraphicsGetImageFromCurrentImageContext() ?: this
  } finally {
    UIGraphicsEndImageContext()
  }
}

private const val AppIconSizePx = 64.0
private const val AppIconJpegQuality = 0.8
