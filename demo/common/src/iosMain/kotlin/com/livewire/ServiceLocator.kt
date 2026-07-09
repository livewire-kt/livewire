package com.livewire

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.livewire.app.LivewireDatabase
import com.livewire.client.LivewireClient
import com.livewire.plugin.database.DatabasePlugin
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.plugin.recomposition.RecompositionPlugin
import com.livewire.theme.CustomLivewireTheme

object ServiceLocator {
  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin())
      install(NetworkPlugin())
      install(PlaygroundPlugin())
      install(RecompositionPlugin())
    }
  }

  val database by lazy {
    val driver = NativeSqliteDriver(
      LivewireDatabase.Schema.synchronous(),
      "livewire.db",
    )

    LivewireDatabase(driver)
  }
}
