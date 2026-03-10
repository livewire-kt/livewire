package com.r0adkll.livewire.runtime

import java.util.prefs.Preferences

object DevicePreferences {
  private val prefs = Preferences.userNodeForPackage(DevicePreferences::class.java)

  var lastConnectedDeviceId: String?
    get() = prefs.get(LastDeviceIdKey, null)
    set(value) {
      if (value != null) {
        prefs.put(LastDeviceIdKey, value)
      } else {
        prefs.remove(LastDeviceIdKey)
      }
    }

  private const val LastDeviceIdKey = "last_connected_device_id"
}
