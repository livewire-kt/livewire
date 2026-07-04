package com.livewire.client

import android.os.Build
import android.os.Process
import com.livewire.ContextHolder
import com.livewire.discovery.DiscoveryPacket

actual fun createDiscoveryConfig(instanceId: String): DiscoveryConfig {
  val context = ContextHolder.appContext

  return DiscoveryConfig(
    packet = DiscoveryPacket(
      instanceId = instanceId,
      appName = context.applicationInfo.loadLabel(context.packageManager).toString(),
      packageName = context.packageName,
      processId = Process.myPid().toLong(),
      platform = Android,
      deviceName = Build.MODEL,
      osVersion = Build.VERSION.RELEASE,
    ),
    transport = Tcp,
  )
}
