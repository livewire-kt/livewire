package com.r0adkll.livewire

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
  if (!isSimulator()) {
    DisposableEffect(Unit) {
      val portForwarder = PortForwarder(
        forwardPort = LivewireConstants.Port.toUInt(),
        bridgePort = LivewireConstants.BridgePort.toUInt(),
      )
      portForwarder.start()

      onDispose {
        portForwarder.stop()
      }
    }
  }

  MaterialTheme {
    LivewireApp(
      livewireClient = ServiceLocator.livewireClient,
    )
  }
}

private fun isSimulator(): Boolean {
  val env = NSProcessInfo.processInfo.environment
  return env["SIMULATOR_DEVICE_NAME"] != null
}
