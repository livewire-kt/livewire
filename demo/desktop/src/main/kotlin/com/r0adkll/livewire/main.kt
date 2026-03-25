package com.r0adkll.livewire

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
  val livewireClient = remember { ServiceLocator.livewireClient }

  LaunchedEffect(Unit) {
    DemoDbConfigurator.populate(ServiceLocator.database)
  }

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
