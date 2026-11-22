package com.livewire

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val livewireClient = ServiceLocator.livewireClient

  ComposeViewport(viewportContainerId = "composeApplication") {
    LivewireApp(
      livewireClient = livewireClient,
    )
  }
}
