package com.livewire.runtime.discoverymanager

sealed interface HostApp {
  val id: String
  val instanceId: String
  val displayName: String
  val device: HostDevice
  val appIcon: ByteArray?
  val protocolVersion: Int
}

data class AndroidApp(
  override val instanceId: String,
  val packageName: String,
  val label: String,
  override val device: AdbDevice,
  override val appIcon: ByteArray? = null,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "android:${device.serial}:$packageName"
  override val displayName: String = label.ifEmpty { packageName }
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AndroidApp

    if (instanceId != other.instanceId) return false
    if (packageName != other.packageName) return false
    if (label != other.label) return false
    if (device != other.device) return false
    if (!appIcon.contentEquals(other.appIcon)) return false
    if (id != other.id) return false
    if (displayName != other.displayName) return false
    if (protocolVersion != other.protocolVersion) return false

    return true
  }

  override fun hashCode(): Int {
    var result = instanceId.hashCode()
    result = 31 * result + packageName.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + device.hashCode()
    result = 31 * result + (appIcon?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + protocolVersion.hashCode()
    return result
  }


}

data class IosApp(
  override val instanceId: String,
  val appName: String,
  val bundleId: String,
  override val device: IosDevice,
  override val appIcon: ByteArray? = null,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "ios:${device.udid}:$appName"
  override val displayName: String = appName
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as IosApp

    if (instanceId != other.instanceId) return false
    if (appName != other.appName) return false
    if (bundleId != other.bundleId) return false
    if (device != other.device) return false
    if (!appIcon.contentEquals(other.appIcon)) return false
    if (id != other.id) return false
    if (displayName != other.displayName) return false
    if (protocolVersion != other.protocolVersion) return false
    
    return true
  }

  override fun hashCode(): Int {
    var result = instanceId.hashCode()
    result = 31 * result + appName.hashCode()
    result = 31 * result + bundleId.hashCode()
    result = 31 * result + device.hashCode()
    result = 31 * result + (appIcon?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + protocolVersion.hashCode()
    return result
  }


}

data class WebApp(
  override val instanceId: String,
  val appName: String,
  val pageOrigin: String,
  val browser: String,
  override val appIcon: ByteArray? = null,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "web:$pageOrigin:$instanceId"
  override val displayName: String = appName.ifEmpty { pageOrigin }
  override val device: HostDevice = WebDevice(browser)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WebApp

    if (instanceId != other.instanceId) return false
    if (appName != other.appName) return false
    if (pageOrigin != other.pageOrigin) return false
    if (browser != other.browser) return false
    if (!appIcon.contentEquals(other.appIcon)) return false
    if (id != other.id) return false
    if (displayName != other.displayName) return false
    if (device != other.device) return false
    if (protocolVersion != other.protocolVersion) return false

    return true
  }

  override fun hashCode(): Int {
    var result = instanceId.hashCode()
    result = 31 * result + appName.hashCode()
    result = 31 * result + pageOrigin.hashCode()
    result = 31 * result + browser.hashCode()
    result = 31 * result + (appIcon?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + device.hashCode()
    result = 31 * result + protocolVersion.hashCode()
    return result
  }
}

data class DesktopApp(
  override val instanceId: String,
  val appName: String,
  val processId: Long,
  override val appIcon: ByteArray? = null,
  override val protocolVersion: Int,
) : HostApp {
  override val id: String = "desktop:$appName"
  override val displayName: String = appName
  override val device: HostDevice = DesktopDevice
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DesktopApp

    if (processId != other.processId) return false
    if (instanceId != other.instanceId) return false
    if (appName != other.appName) return false
    if (!appIcon.contentEquals(other.appIcon)) return false
    if (id != other.id) return false
    if (displayName != other.displayName) return false
    if (device != other.device) return false
    if (protocolVersion != other.protocolVersion) return false

    return true
  }

  override fun hashCode(): Int {
    var result = processId.hashCode()
    result = 31 * result + instanceId.hashCode()
    result = 31 * result + appName.hashCode()
    result = 31 * result + (appIcon?.contentHashCode() ?: 0)
    result = 31 * result + id.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + device.hashCode()
    result = 31 * result + protocolVersion.hashCode()
    return result
  }


}
