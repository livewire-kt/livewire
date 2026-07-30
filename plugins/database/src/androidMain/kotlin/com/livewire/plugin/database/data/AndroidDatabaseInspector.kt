package com.livewire.plugin.database.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDatabaseInspector(
  private val context: Context,
) : DatabaseInspector {

  override suspend fun discoverDatabases(): Result<List<DatabaseInfo>> = withContext(Dispatchers.IO) {
    try {
      val databases = context.databaseList()
        .map { name -> context.getDatabasePath(name) }
        .filter { file -> file.isFile && file.isSqliteDatabase() }
        .map { file ->
          DatabaseInfo(
            name = file.name,
            path = file.absolutePath,
            sizeBytes = file.length(),
          )
        }
      Result.success(databases)
    } catch (e: Exception) {
      Result.failure(Exception("Failed to discover databases", e))
    }
  }

  override suspend fun <T> withDatabase(
    path: String,
    readOnly: Boolean,
    block: (DatabaseInspector.DatabaseConnection) -> T,
  ): Result<T> {
    return try {
      val flags = if (readOnly) {
        SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
      } else {
        SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.NO_LOCALIZED_COLLATORS
      }
      val db = SQLiteDatabase.openDatabase(path, null, flags)
      try {
        Result.success(db.use { block(AndroidConnection(it)) })
      } catch (e: Exception) {
        Result.failure(Exception("query failed: ${e.message}", e))
      }
    } catch (e: Exception) {
      Result.failure(Exception("failed to open database: ${e.message}", e))
    }
  }

  private fun File.isSqliteDatabase(): Boolean = try {
    inputStream().use { stream ->
      val header = ByteArray(SQLITE_MAGIC.size)
      var read = 0
      while (read < header.size) {
        val count = stream.read(header, read, header.size - read)
        if (count == -1) break
        read += count
      }
      isSqliteHeader(header.copyOf(read))
    }
  } catch (e: IOException) {
    false
  }

  private class AndroidConnection(
    private val db: SQLiteDatabase,
  ) : DatabaseInspector.DatabaseConnection {

    override fun rawQuery(sql: String): QueryResult {
      return db.rawQuery(sql, null).use { cursor ->
        cursorToQueryResult(cursor)
      }
    }

    override fun execSql(sql: String) {
      db.execSQL(sql)
    }

    private fun cursorToQueryResult(cursor: Cursor): QueryResult {
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
        executionTimeMs = 0,
      )
    }
  }
}
