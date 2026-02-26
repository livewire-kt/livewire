package com.r0adkll.livewire

import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.plugin.database.DatabasePlugin
import com.r0adkll.livewire.plugin.playground.PlaygroundPlugin
import com.r0adkll.livewire.ui.theme.CustomLivewireTheme

object ServiceLocator {
  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin())
      install(PlaygroundPlugin())
    }
  }
}
