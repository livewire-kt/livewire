package com.livewire.plugin.database.data

/** Every SQLite database file begins with these 16 bytes: "SQLite format 3" plus a NUL terminator. */
internal val SQLITE_MAGIC = "SQLite format 3".encodeToByteArray() + 0.toByte()

/**
 * Returns true if [header] starts with the SQLite file magic. Callers pass the first
 * 16 bytes of a candidate file; matching on content instead of file extension finds
 * databases regardless of naming (`.db`, `.sqlite`, no extension) while excluding
 * `-journal`/`-wal`/`-shm` sidecar files, which have different magic.
 */
internal fun isSqliteHeader(header: ByteArray): Boolean {
  if (header.size < SQLITE_MAGIC.size) return false
  return SQLITE_MAGIC.indices.all { header[it] == SQLITE_MAGIC[it] }
}
