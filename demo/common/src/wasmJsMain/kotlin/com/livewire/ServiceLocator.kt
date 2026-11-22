package com.livewire

import com.livewire.client.LivewireClient
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.theme.CustomLivewireTheme
import com.livewire.ui.data.LayoutNodeSerialization

object ServiceLocator {
  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      // No DatabasePlugin or RecompositionPlugin on web: browsers have no filesystem for the
      // database inspector to scan, and the recomposition tracker is JVM/Android-only.
      install(NetworkPlugin())
      install(PlaygroundPlugin())

      layoutNodeSerialization(LayoutNodeSerialization.Json)
      debugLogging(true)
    }
  }
}
