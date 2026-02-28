package com.r0adkll.livewire

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
  MaterialTheme {
    LivewireApp(
      livewireClient = ServiceLocator.livewireClient,
    )
  }
}
