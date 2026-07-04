package com.livewire

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.livewire.plugin.recomposition.RecompositionPlugin

fun main() {
  RecompositionPlugin.init()

  application {
    val livewireClient = remember { ServiceLocator.livewireClient }

    Window(
      onCloseRequest = {
        livewireClient.stop()
        exitApplication()
      },
      title = "Livewire Client",
      state = rememberWindowState(
        size = DpSize(400.dp, 800.dp),
      ),
    ) {
      LivewireApp(
        livewireClient = livewireClient,
      )
    }
  }
}
