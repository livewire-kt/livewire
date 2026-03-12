package com.r0adkll.livewire

import android.app.Application
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.r0adkll.livewire.app.Projects
import com.r0adkll.livewire.app.Tasks
import com.r0adkll.livewire.app.Users
import com.r0adkll.livewire.app.UsersQueries
import com.r0adkll.livewire.fakes.FakeTasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

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

    // Populate DB for testing
    with (ServiceLocator.database) {
      scope.launch {
        val count = usersQueries.count().awaitAsOneOrNull() ?: 0L
        if (count == 0L) {
          usersQueries.insert(Users(0, "James"))
          usersQueries.insert(Users(1, "Janet"))
          usersQueries.insert(Users(2, "Jamal"))

          projectsQueries.insert(Projects(0, "Kotlin", 2, 0))
          projectsQueries.insert(Projects(1, "Go", 6, 1))
          projectsQueries.insert(Projects(2, "Rust", 6, 2))

          FakeTasks.tasks.forEachIndexed { index, task ->
            tasksQueries.insert(Tasks(index.toLong(), task, Random.nextLong(), Random.nextInt(0, 3).toLong()))
          }
        }
      }
    }
  }
}
