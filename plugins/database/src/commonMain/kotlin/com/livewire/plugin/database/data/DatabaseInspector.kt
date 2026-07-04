package com.livewire.plugin.database.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

interface DatabaseInspector {
  interface DatabaseConnection {
    fun rawQuery(sql: String): QueryResult
    fun execSql(sql: String)
  }

  suspend fun discoverDatabases(): Result<List<DatabaseInfo>>

  suspend fun <T> withDatabase(
    path: String,
    readOnly: Boolean = true,
    block: (DatabaseConnection) -> T,
  ): Result<T>

  suspend fun getTables(databasePath: String): Result<List<TableInfo>> = withContext(Dispatchers.IO) {
    withDatabase(databasePath) { connection ->
      buildList {
        val masterResult = connection.rawQuery(
          "SELECT name, type FROM sqlite_master WHERE type IN ('table', 'view') AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' ORDER BY name",
        )

        for (row in masterResult.rows) {
          val name = row[0] ?: continue
          val type = row[1] ?: "table"

          val pragmaResult = connection.rawQuery("PRAGMA table_info(\"$name\")")
          val columns = pragmaResult.rows.map { pragmaRow ->
            ColumnInfo(
              index = pragmaRow[0]?.toIntOrNull() ?: 0,
              name = pragmaRow[1] ?: "",
              type = pragmaRow[2] ?: "",
              notNull = pragmaRow[3] == "1",
              defaultValue = pragmaRow[4],
              primaryKey = pragmaRow[5] == "1",
            )
          }

          add(TableInfo(name = name, type = type, columns = columns))
        }
      }
    }
  }

  suspend fun executeQuery(databasePath: String, sql: String): Result<QueryResult> = withContext(Dispatchers.IO) {
    val trimmed = sql.trim()
    val isReadOnly = trimmed.startsWith("SELECT", ignoreCase = true)
      || trimmed.startsWith("PRAGMA", ignoreCase = true)
      || trimmed.startsWith("EXPLAIN", ignoreCase = true)

    val mark = TimeSource.Monotonic.markNow()

    if (isReadOnly) {
      withDatabase(databasePath) { conn ->
        conn.rawQuery(trimmed)
          .copy(executionTimeMs = mark.elapsedNow().inWholeMilliseconds)
      }
    } else {
      withDatabase(databasePath, readOnly = false) { conn ->
        conn.execSql(trimmed)

        QueryResult(
          columns = listOf("result"),
          rows = listOf(listOf("Statement executed successfully")),
          rowCount = 1,
          executionTimeMs = mark.elapsedNow().inWholeMilliseconds,
        )
      }
    }
  }

  suspend fun getTableContents(
    databasePath: String,
    tableName: String,
    limit: Int = 500,
  ): Result<QueryResult> = withContext(Dispatchers.IO) {
    withDatabase(databasePath) { conn ->
      val mark = TimeSource.Monotonic.markNow()
      conn.rawQuery("SELECT * FROM \"$tableName\" LIMIT $limit")
        .copy(executionTimeMs = mark.elapsedNow().inWholeMilliseconds)
    }
  }
}
