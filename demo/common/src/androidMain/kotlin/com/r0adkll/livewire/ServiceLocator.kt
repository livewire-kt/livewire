package com.r0adkll.livewire

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.r0adkll.livewire.app.LivewireDatabase
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.plugin.database.DatabasePlugin
import com.r0adkll.livewire.plugin.network.NetworkPlugin
import com.r0adkll.livewire.plugin.playground.PlaygroundPlugin
import com.r0adkll.livewire.ui.data.LayoutNodeSerialization
import com.r0adkll.livewire.ui.theme.CustomLivewireTheme

@SuppressLint("StaticFieldLeak")
object ServiceLocator {
  lateinit var context: Context

  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin(context))
      install(NetworkPlugin())
      install(PlaygroundPlugin())

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
