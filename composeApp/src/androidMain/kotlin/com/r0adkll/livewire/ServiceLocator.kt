package com.r0adkll.livewire

import android.annotation.SuppressLint
import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.r0adkll.livewire.app.LivewireDatabase
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.plugin.database.DatabasePlugin
import com.r0adkll.livewire.plugin.playground.PlaygroundPlugin

@SuppressLint("StaticFieldLeak")
object ServiceLocator {
  lateinit var context: Context

  val livewireClient by lazy {
    LivewireClient {
      install(DatabasePlugin(context))
      install(PlaygroundPlugin())
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
