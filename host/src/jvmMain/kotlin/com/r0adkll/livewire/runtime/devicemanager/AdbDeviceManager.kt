@file:OptIn(ExperimentalAtomicApi::class)

package com.r0adkll.livewire.runtime.devicemanager

import dadb.Dadb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class AdbDevice(
  val connection: Dadb,
  val serial: String,
  val model: String,
) : HostDevice {
  override val id: String = "android:$serial"
  override val displayName: String = model.ifEmpty { serial }
}

object AdbDeviceManager : PlatformDeviceManager {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val started = AtomicBoolean(false)

  final override val devices: Flow<List<HostDevice>>
    field = MutableStateFlow<List<HostDevice>>(emptyList())

  final override val isReady: StateFlow<Boolean>
    field = MutableStateFlow(false)

  override suspend fun ensureStarted() {
    if (started.compareAndSet(expectedValue = false, newValue = true)) {
      scope.launch {
        while (true) {
          val result = runCatching {
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

          result.getOrNull()?.let { devices.value = it }
          isReady.value = true
          delay(RefreshRateMs)
        }
      }
    }
  }

  override fun shutdown() {
    scope.cancel()
    devices.value = emptyList()
  }
}

private const val RefreshRateMs = 2000L
