package com.r0adkll.livewire

import android.content.Context
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.plugin.database.DatabasePlugin

object ServiceLocator {
  lateinit var context: Context

  val livewireClient by lazy {
    LivewireClient {
      install(DatabasePlugin())
    }
  }

}
