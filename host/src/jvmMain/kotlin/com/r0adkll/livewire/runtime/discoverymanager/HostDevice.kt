package com.r0adkll.livewire.runtime.discoverymanager

import dadb.Dadb

sealed interface HostDevice {
  val id: String
  val displayName: String
  val displayDetail: String
}

data class AdbDevice(
  val connection: Dadb,
  val serial: String,
  val model: String,
  val apiVersion: String,
) : HostDevice {
  override val id: String = "android:$serial"
  override val displayName: String = model.ifEmpty { serial }
  override val displayDetail: String = "API $apiVersion"
}

data class IosDevice(
  val connection: IosDeviceConnection,
  val udid: String,
  val name: String,
  val deviceType: DeviceType,
  val osVersion: String,
) : HostDevice {
  override val id: String = "ios:$udid"
  override val displayName: String = name
  override val displayDetail: String = "iOS $osVersion"

  enum class DeviceType {
    Physical,
    Simulator,
  }
}

object DesktopDevice : HostDevice {
  override val id: String = "desktop:local"
  override val displayName: String = "Desktop Apps"
  override val displayDetail: String = "Local Machine"
}
