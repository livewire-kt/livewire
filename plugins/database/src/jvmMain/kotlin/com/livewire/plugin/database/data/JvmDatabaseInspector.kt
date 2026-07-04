package com.livewire.plugin.database.data

import java.io.File
import java.sql.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sqlite.SQLiteConfig

class JvmDatabaseInspector(private val searchDirectories: List<File>) : DatabaseInspector {
  override suspend fun discoverDatabases(): Result<List<DatabaseInfo>> = withContext(Dispatchers.IO) {
    try {
      val databases = mutableListOf<DatabaseInfo>()
      for (dir in searchDirectories) {
        if (dir.isDirectory) {
          collectDatabases(dir, databases)
        }
      }
      Result.success(databases)
    } catch (e: Exception) {
      Result.failure(Exception("Failed to discover databases", e))
    }
  }

  private fun collectDatabases(dir: File, out: MutableList<DatabaseInfo>) {
    val files = dir.listFiles() ?: return
    for (file in files) {
      if (file.isFile && file.name.endsWith(".db")) {
        out += DatabaseInfo(
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
        )
      } else if (file.isDirectory) {
        collectDatabases(file, out)
      }
    }
  }

  override suspend fun <T> withDatabase(
    path: String,
    readOnly: Boolean,
    block: (DatabaseInspector.DatabaseConnection) -> T,
  ): Result<T> {
    return try {
      SQLiteConfig()
        .also { it.setReadOnly(readOnly) }
        .createConnection("jdbc:sqlite:$path").use { connection ->
          try {
            Result.success(block(JdbcConnection(connection)))
          } catch (e: Exception) {
            Result.failure(Exception("query failed: ${e.message}", e))
          }
        }
    } catch (e: Exception) {
      Result.failure(Exception("failed to open database: ${e.message}", e))
    }
  }

  private class JdbcConnection(private val connection: Connection) : DatabaseInspector.DatabaseConnection {
    override fun rawQuery(sql: String): QueryResult {
      return connection.createStatement().use { statement ->
        val result = statement.executeQuery(sql)

        val columnCount = result.metaData.columnCount
        val columns = (1..columnCount).map { i -> result.metaData.getColumnName(i) }

        val rows = buildList {
          while (result.next()) {
            val row = (1..columnCount).map { result.getString(it) }
            add(row)
          }
        }

        return QueryResult(
          columns = columns,
          rows = rows,
          rowCount = rows.size,
          executionTimeMs = 0,
        )
      }
    }

    override fun execSql(sql: String) {
      connection.createStatement().use { it.execute(sql) }
    }
  }
}
