package com.r0adkll.livewire

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LivewireApplication : Application() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate() {
    super.onCreate()
    // Poor man's "DI"
    ServiceLocator.context = this

    // Copy packaged campfire.db from assets if it doesn't exist yet
    val dbFile = getDatabasePath("campfire.db")
    if (!dbFile.exists()) {
      dbFile.parentFile?.mkdirs()
      assets.open("databases/campfire.db").use { input ->
        dbFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }
    }

    scope.launch {
      DemoDbConfigurator.populate(ServiceLocator.database)
    }
  }
}
