package com.livewire

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.livewire.app.LivewireDatabase
import com.livewire.client.LivewireClient
import com.livewire.plugin.database.DatabasePlugin
import com.livewire.plugin.network.NetworkPlugin
import com.livewire.plugin.playground.PlaygroundPlugin
import com.livewire.plugin.recomposition.RecompositionPlugin
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.theme.CustomLivewireTheme

object ServiceLocator {
  val livewireClient by lazy {
    LivewireClient {
      theme(CustomLivewireTheme)
      install(DatabasePlugin("."))
      install(NetworkPlugin())
      install(PlaygroundPlugin())
      install(RecompositionPlugin())

      layoutNodeSerialization(LayoutNodeSerialization.Json)
      debugLogging(true)
    }
  }

  val database by lazy {
    val driver = JdbcSqliteDriver("jdbc:sqlite:livewire.db")

    val oldVersion = driver.executeQuery(
      identifier = null,
      sql = "PRAGMA $VersionPragma;",
      mapper = {
        QueryResult.Value(it.getLong(0))
      },
      parameters = 0,
      binders = null,
    ).value ?: 0L

    val schema = LivewireDatabase.Schema.synchronous()
    when {
      oldVersion == 0L -> driver.createDb(schema)
      oldVersion < schema.version -> driver.migrateDb(oldVersion, schema)
      oldVersion > schema.version -> error("Database version $oldVersion is newer than the current version ${schema.version}")
    }

    driver.execute(null, "PRAGMA foreign_keys=ON;", 0)

    LivewireDatabase(driver)
  }

  private fun SqlDriver.createDb(schema: SqlSchema<QueryResult.Value<Unit>>) {
    schema.create(this)
    setVersionPragma(schema.version)
  }

  private fun SqlDriver.migrateDb(oldVersion: Long, schema: SqlSchema<QueryResult.Value<Unit>>) {
    schema.migrate(this, oldVersion, schema.version)
    setVersionPragma(schema.version)
  }

  private fun SqlDriver.setVersionPragma(version: Long) {
    execute(null, "PRAGMA $VersionPragma = $version;", 0, null)
  }
}

private const val VersionPragma = "user_version"

