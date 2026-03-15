package com.r0adkll.livewire.plugin.database.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDatabaseInspector(
  private val context: Context,
) : DatabaseInspector {

  override suspend fun discoverDatabases(): Result<List<DatabaseInfo>> = withContext(Dispatchers.IO) {
    try {
      val databases = context.databaseList()
        .filter { it.endsWith(".db") }
        .map { name ->
          val file = context.getDatabasePath(name)
          DatabaseInfo(
            name = name,
            path = file.absolutePath,
            sizeBytes = file.length(),
          )
        }
      Result.success(databases)
    } catch (e: Exception) {
      Result.failure(Exception("Failed to discover databases", e))
    }
  }

  override suspend fun getTables(databasePath: String): Result<List<TableInfo>> = withContext(Dispatchers.IO) {
    withDatabase(databasePath) { db ->
      val tables = mutableListOf<TableInfo>()

      db.rawQuery(
        "SELECT name, type FROM sqlite_master WHERE type IN ('table', 'view') AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' ORDER BY name",
        null,
      ).use { cursor ->
        while (cursor.moveToNext()) {
          val name = cursor.getString(0)
          val type = cursor.getString(1)

          val columns = mutableListOf<ColumnInfo>()
          db.rawQuery("PRAGMA table_info(\"$name\")", null).use { info ->
            while (info.moveToNext()) {
              columns += ColumnInfo(
                index = info.getInt(0),
                name = info.getString(1),
                type = info.getString(2) ?: "",
                notNull = info.getInt(3) != 0,
                defaultValue = if (info.isNull(4)) null else info.getString(4),
                primaryKey = info.getInt(5) != 0,
              )
            }
          }

          tables += TableInfo(
            name = name,
            type = type,
            columns = columns,
          )
        }
      }

      tables
    }
  }

  override suspend fun executeQuery(databasePath: String, sql: String): Result<QueryResult> = withContext(Dispatchers.IO) {
    val trimmed = sql.trim()
    val isSelect = trimmed.startsWith("SELECT", ignoreCase = true)
      || trimmed.startsWith("PRAGMA", ignoreCase = true)
      || trimmed.startsWith("EXPLAIN", ignoreCase = true)

    if (isSelect) {
      withDatabase(databasePath) { db ->
        val startTime = System.currentTimeMillis()
        db.rawQuery(trimmed, null).use { cursor ->
          cursorToQueryResult(cursor, startTime)
        }
      }
    } else {
      withDatabase(databasePath, readOnly = false) { db ->
        val startTime = System.currentTimeMillis()
        db.execSQL(trimmed)
        val elapsed = System.currentTimeMillis() - startTime
        QueryResult(
          columns = listOf("result"),
          rows = listOf(listOf("Statement executed successfully")),
          rowCount = 1,
          executionTimeMs = elapsed,
        )
      }
    }
  }

  override suspend fun getTableContents(
    databasePath: String,
    tableName: String,
    limit: Int,
  ): Result<QueryResult> = withContext(Dispatchers.IO) {
    withDatabase(databasePath) { db ->
      val startTime = System.currentTimeMillis()
      db.rawQuery("SELECT * FROM \"$tableName\" LIMIT ?", arrayOf(limit.toString())).use { cursor ->
        cursorToQueryResult(cursor, startTime)
      }
    }
  }

  private fun cursorToQueryResult(cursor: Cursor, startTime: Long): QueryResult {
    val columns = cursor.columnNames.toList()
    val rows = mutableListOf<List<String?>>()
    while (cursor.moveToNext()) {
      val row = (0 until cursor.columnCount).map { i ->
        if (cursor.isNull(i)) null else cursor.getString(i)
      }
      rows += row
    }
    return QueryResult(
      columns = columns,
      rows = rows,
      rowCount = rows.size,
      executionTimeMs = System.currentTimeMillis() - startTime,
    )
  }

  private inline fun <T> withDatabase(
    path: String,
    readOnly: Boolean = true,
    block: (SQLiteDatabase) -> T,
  ): Result<T> {
    return try {
      val flags = if (readOnly) {
        SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
      } else {
        SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS
      }
      val db = SQLiteDatabase.openDatabase(path, null, flags)
      try {
        Result.success(db.use { block(it) })
      } catch (e: Exception) {
        Result.failure(Exception("Query failed: ${e.message}", e))
      }
    } catch (e: Exception) {
      Result.failure(Exception("Failed to open database: ${e.message}", e))
    }
  }
}
