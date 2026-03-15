@file:OptIn(ExperimentalForeignApi::class)

package com.r0adkll.livewire.plugin.database.data

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import cnames.structs.sqlite3
import cnames.structs.sqlite3_stmt
import csqlite3.SQLITE_OK
import csqlite3.SQLITE_OPEN_READONLY
import csqlite3.SQLITE_OPEN_READWRITE
import csqlite3.SQLITE_ROW
import csqlite3.sqlite3_close
import csqlite3.sqlite3_column_count
import csqlite3.sqlite3_column_name
import csqlite3.sqlite3_column_text
import csqlite3.sqlite3_errmsg
import csqlite3.sqlite3_exec
import csqlite3.sqlite3_finalize
import csqlite3.sqlite3_open_v2
import csqlite3.sqlite3_prepare_v2
import csqlite3.sqlite3_step
import kotlin.time.TimeSource

class IosDatabaseInspector : DatabaseInspector {

  override suspend fun discoverDatabases(): Result<List<DatabaseInfo>> = withContext(Dispatchers.IO) {
    try {
      val databases = mutableListOf<DatabaseInfo>()

      val searchDirs = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true)
      for (dir in searchDirs) {
        val dirPath = dir as? String ?: continue
        collectDatabases(NSFileManager.defaultManager, dirPath, databases)
      }

      Result.success(databases)
    } catch (e: Exception) {
      Result.failure(Exception("Failed to discover databases", e))
    }
  }

  private fun collectDatabases(
    fileManager: NSFileManager,
    dirPath: String,
    out: MutableList<DatabaseInfo>,
  ) {
    val contents = fileManager.contentsOfDirectoryAtPath(dirPath, error = null) ?: return
    for (item in contents) {
      val name = item as? String ?: continue
      val fullPath = "$dirPath/$name"

      if (!fileManager.fileExistsAtPath(fullPath)) continue

      if (name.endsWith(".db")) {
        val attrs = fileManager.attributesOfItemAtPath(fullPath, error = null)
        val size = (attrs?.get(NSFileSize) as? Number)?.toLong() ?: 0L
        out += DatabaseInfo(
          name = name,
          path = fullPath,
          sizeBytes = size,
        )
      }

      val subContents = fileManager.contentsOfDirectoryAtPath(fullPath, error = null)
      if (subContents != null && !name.endsWith(".db")) {
        collectDatabases(fileManager, fullPath, out)
      }
    }
  }

  override suspend fun getTables(databasePath: String): Result<List<TableInfo>> = withContext(Dispatchers.IO) {
    withDatabase(databasePath) { db ->
      buildList {
        val queryResult = rawQuery(
          db = db,
          sql = "SELECT name, type FROM sqlite_master WHERE type IN ('table', 'view') AND name NOT LIKE 'sqlite_%' ORDER BY name"
        )

        for (row in queryResult.rows) {
          val name = row[0] ?: continue
          val type = row[1] ?: "table"

          val pragmaResult = rawQuery(db, "PRAGMA table_info(\"$name\")")
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

          add(
            TableInfo(
              name = name,
              type = type,
              columns = columns,
            ),
          )
        }
      }
    }
  }

  override suspend fun executeQuery(databasePath: String, sql: String): Result<QueryResult> = withContext(Dispatchers.IO) {
    val trimmed = sql.trim()
    val isSelect = trimmed.startsWith("SELECT", ignoreCase = true)
      || trimmed.startsWith("PRAGMA", ignoreCase = true)
      || trimmed.startsWith("EXPLAIN", ignoreCase = true)

    if (isSelect) {
      withDatabase(databasePath) { db ->
        val mark = TimeSource.Monotonic.markNow()
        val result = rawQuery(db, trimmed)
        result.copy(executionTimeMs = mark.elapsedNow().inWholeMilliseconds)
      }
    } else {
      withDatabase(databasePath, readOnly = false) { db ->
        val mark = TimeSource.Monotonic.markNow()
        execSql(db, trimmed)
        val elapsed = mark.elapsedNow().inWholeMilliseconds
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
      val mark = TimeSource.Monotonic.markNow()
      val result = rawQuery(db, "SELECT * FROM \"$tableName\" LIMIT $limit")
      result.copy(executionTimeMs = mark.elapsedNow().inWholeMilliseconds)
    }
  }

  private fun rawQuery(db: CPointer<sqlite3>, sql: String): QueryResult = memScoped {
    val stmtPtr = alloc<CPointerVar<sqlite3_stmt>>()
    val rc = sqlite3_prepare_v2(db, sql, -1, stmtPtr.ptr, null)
    if (rc != SQLITE_OK) {
      val errMsg = sqlite3_errmsg(db)?.toKString() ?: "Unknown error"
      throw Exception("failed to prepare statement: $errMsg")
    }
    val stmt = stmtPtr.value ?: throw Exception("null statement after prepare")

    try {
      val columnCount = sqlite3_column_count(stmt)
      val columns = (0 until columnCount).map { i ->
        sqlite3_column_name(stmt, i)?.reinterpret<ByteVar>()?.toKString() ?: ""
      }

      val rows = mutableListOf<List<String?>>()
      while (sqlite3_step(stmt) == SQLITE_ROW) {
        val row = (0 until columnCount).map { i ->
          sqlite3_column_text(stmt, i)?.reinterpret<ByteVar>()?.toKString()
        }
        rows += row
      }

      QueryResult(
        columns = columns,
        rows = rows,
        rowCount = rows.size,
        executionTimeMs = 0,
      )
    } finally {
      sqlite3_finalize(stmt)
    }
  }

  private fun execSql(db: CPointer<sqlite3>, sql: String) {
    val rc = sqlite3_exec(db, sql, null, null, null)
    if (rc != SQLITE_OK) {
      val errMsg = sqlite3_errmsg(db)?.toKString() ?: "Unknown error"
      throw Exception("Failed to execute statement: $errMsg")
    }
  }

  private inline fun <T> withDatabase(
    path: String,
    readOnly: Boolean = true,
    block: (CPointer<sqlite3>) -> T,
  ): Result<T> = memScoped {
    val dbPtr = alloc<CPointerVar<sqlite3>>()
    val flags = if (readOnly) SQLITE_OPEN_READONLY else SQLITE_OPEN_READWRITE
    val rc = sqlite3_open_v2(path, dbPtr.ptr, flags, null)
    if (rc != SQLITE_OK) {
      val errMsg = sqlite3_errmsg(dbPtr.value)?.toKString() ?: "unknown error"
      sqlite3_close(dbPtr.value)
      return@memScoped Result.failure(Exception("failed to open database: $errMsg"))
    }
    val db = dbPtr.value ?: return@memScoped Result.failure(Exception("null database pointer"))
    try {
      Result.success(block(db))
    } catch (e: Exception) {
      Result.failure(Exception("query failed: ${e.message}", e))
    } finally {
      sqlite3_close(db)
    }
  }
}
