package com.livewire.runtime.discoverymanager

import androidx.compose.ui.graphics.vector.ImageVector
import com.livewire.ui.icons.AndroidIcon
import com.livewire.ui.icons.AppleIcon
import com.livewire.ui.icons.DesktopIcon
import dadb.Dadb

sealed interface HostDevice {
  val id: String
  val displayDetail: String
  val platformIcon: ImageVector
}

data class AdbDevice(
  val connection: Dadb,
  val serial: String,
  val model: String,
  val apiVersion: String,
) : HostDevice {
  override val id: String = "android:$serial"
  override val displayDetail: String = "${model.ifEmpty { serial }} • API $apiVersion"
  override val platformIcon: ImageVector = AndroidIcon
}

data class IosDevice(
  val connection: IosDeviceConnection,
  val udid: String,
  val name: String,
  val deviceType: DeviceType,
  val osVersion: String,
) : HostDevice {
  override val id: String = "ios:$udid"
  override val displayDetail: String = "$name • iOS $osVersion"
  override val platformIcon: ImageVector = AppleIcon

  enum class DeviceType {
    Physical,
    Simulator,
  }
}

object DesktopDevice : HostDevice {
  override val id: String = "desktop:local"
  override val displayDetail: String = "Local Machine"
  override val platformIcon: ImageVector = DesktopIcon
}
