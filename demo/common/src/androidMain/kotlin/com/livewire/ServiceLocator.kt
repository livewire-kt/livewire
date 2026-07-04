package com.livewire

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.livewire.app.LivewireDatabase
import com.livewire.client.LivewireClient
import com.livewire.plugin.database.DatabasePlugin
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.plugin.recomposition.RecompositionPlugin
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.ui.theme.CustomLivewireTheme

@SuppressLint("StaticFieldLeak")
object ServiceLocator {
  lateinit var context: Context

  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin(context))
      install(NetworkPlugin())
      install(PlaygroundPlugin())
      install(RecompositionPlugin())

      layoutNodeSerialization(LayoutNodeSerialization.Json)
    }
  }

  val database by lazy {
    val driver = AndroidSqliteDriver(
      LivewireDatabase.Schema.synchronous(),
      context,
      "livewire.db",
    )

    LivewireDatabase(driver)
  }
}
