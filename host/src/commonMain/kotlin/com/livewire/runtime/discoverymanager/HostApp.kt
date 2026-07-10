package com.livewire.runtime.discoverymanager

import com.livewire.LivewireConstants

sealed interface HostApp {
  val id: String
  val instanceId: String
  val displayName: String
  val device: HostDevice
  val protocolVersion: Int
}

data class AndroidApp(
  override val instanceId: String,
  val packageName: String,
  val label: String,
  override val device: AdbDevice,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "android:${device.serial}:$packageName"
  override val displayName: String = label.ifEmpty { packageName }
}

data class IosApp(
  override val instanceId: String,
  val appName: String,
  val bundleId: String,
  override val device: IosDevice,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "ios:${device.udid}:$appName"
  override val displayName: String = appName
}

data class DesktopApp(
  override val instanceId: String,
  val appName: String,
  val processId: Long,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "desktop:$appName"
  override val displayName: String = appName
  override val device: HostDevice = DesktopDevice
}
