package com.r0adkll.livewire.runtime

import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AdbDevice(
  val connection: Dadb,
  val serial: String,
  val model: String,
) {
  val displayName: String
    get() = if (model.isNotEmpty() && model != serial) "$model ($serial)" else serial
}

object AdbDeviceManager {

  suspend fun listDevices(): Result<List<AdbDevice>> = withContext(Dispatchers.IO) {
    runCatching {
      Dadb.list().mapNotNull { dadb ->
        try {
          val serial = dadb.shell("getprop ro.serialno").output.trim()
          val model = dadb.shell("getprop ro.product.model").output.trim()
          AdbDevice(
            connection = dadb,
            serial = serial.ifEmpty { "unknown" },
            model = model,
          )
        } catch (_: Exception) {
          null
        } finally {
          dadb.close()
        }
      }
    }
  }
}
