package com.livewire.plugin.database.data

import java.io.File
import java.io.IOException

internal fun scanForDatabases(roots: List<File>): List<DatabaseInfo> {
  val scan = DatabaseScan()
  for (root in roots) {
    scan.visit(root, depth = 0)
  }
  return scan.found
}

private class DatabaseScan {
  val found = mutableListOf<DatabaseInfo>()

  private val visitedDirectories = mutableSetOf<String>()
  private val foundPaths = mutableSetOf<String>()
  private var remainingBudget = MaxFilesScanned

  fun visit(dir: File, depth: Int) {
    if (depth > MaxDepth || remainingBudget <= 0 || !dir.isDirectory) return

    val canonical = dir.canonicalPathOrNull() ?: return
    if (!visitedDirectories.add(canonical)) return

    val entries = dir.listFiles() ?: return
    for (entry in entries) {
      if (remainingBudget <= 0) return
      if (entry.isDirectory) {
        visit(entry, depth + 1)
      } else if (entry.isFile) {
        remainingBudget--
        addIfDatabase(entry)
      }
    }
  }

  private fun addIfDatabase(file: File) {
    if (!file.isSqliteDatabase()) return
    val canonical = file.canonicalPathOrNull() ?: return
    if (!foundPaths.add(canonical)) return

    found += DatabaseInfo(
      name = file.name,
      path = file.absolutePath,
      sizeBytes = file.length(),
    )
  }
}

private fun File.canonicalPathOrNull(): String? = try {
  canonicalPath
} catch (e: IOException) {
  null
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

private const val MaxDepth = 5
private const val MaxFilesScanned = 15000
