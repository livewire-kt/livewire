@file:OptIn(ExperimentalForeignApi::class)

package com.livewire.plugin.database.data

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
        out += DatabaseInfo(
          name = name,
          path = fullPath,
          sizeBytes = (attrs?.get(NSFileSize) as? Number)?.toLong() ?: 0L,
        )
      }

      val subContents = fileManager.contentsOfDirectoryAtPath(fullPath, error = null)
      if (subContents != null && !name.endsWith(".db")) {
        collectDatabases(fileManager, fullPath, out)
      }
    }
  }

  override suspend fun <T> withDatabase(
    path: String,
    readOnly: Boolean,
    block: (DatabaseInspector.DatabaseConnection) -> T,
  ): Result<T> = memScoped {
    val dbPointer = alloc<CPointerVar<sqlite3>>()
    val flags = if (readOnly) SQLITE_OPEN_READONLY else SQLITE_OPEN_READWRITE
    val resultCode = sqlite3_open_v2(path, dbPointer.ptr, flags, null)
    if (resultCode != SQLITE_OK) {
      val errorMessage = sqlite3_errmsg(dbPointer.value)?.toKString() ?: "unknown error"
      sqlite3_close(dbPointer.value)
      return@memScoped Result.failure(Exception("failed to open database: $errorMessage"))
    }
    val db = dbPointer.value ?: return@memScoped Result.failure(Exception("null database pointer"))
    try {
      Result.success(block(NativeConnection(db)))
    } catch (e: Exception) {
      Result.failure(Exception("query failed: ${e.message}", e))
    } finally {
      sqlite3_close(db)
    }
  }

  private class NativeConnection(
    private val db: CPointer<sqlite3>,
  ) : DatabaseInspector.DatabaseConnection {
    override fun rawQuery(sql: String): QueryResult = memScoped {
      val statementPointer = alloc<CPointerVar<sqlite3_stmt>>()
      val resultCode = sqlite3_prepare_v2(db, sql, -1, statementPointer.ptr, null)
      if (resultCode != SQLITE_OK) {
        val errorMessage = sqlite3_errmsg(db)?.toKString() ?: "unknown error"
        throw Exception("failed to prepare statement: $errorMessage")
      }
      val statement = statementPointer.value ?: throw Exception("null statement after prepare")

      try {
        val columnCount = sqlite3_column_count(statement)
        val columns = (0 until columnCount).map { i ->
          sqlite3_column_name(statement, i)?.reinterpret<ByteVar>()?.toKString() ?: ""
        }

        val rows = buildList {
          while (sqlite3_step(statement) == SQLITE_ROW) {
            val row = (0 until columnCount).map { i ->
              sqlite3_column_text(statement, i)?.reinterpret<ByteVar>()?.toKString()
            }
            add(row)
          }
        }

        QueryResult(
          columns = columns,
          rows = rows,
          rowCount = rows.size,
          executionTimeMs = 0,
        )
      } finally {
        sqlite3_finalize(statement)
      }
    }

    override fun execSql(sql: String) {
      val resultCode = sqlite3_exec(db, sql, null, null, null)
      if (resultCode != SQLITE_OK) {
        val errMsg = sqlite3_errmsg(db)?.toKString() ?: "unknown error"
        throw Exception("failed to execute statement: $errMsg")
      }
    }
  }
}
