package com.livewire.plugin.database.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqliteHeaderTest {

  private val magic = "SQLite format 3".encodeToByteArray() + 0.toByte()

  @Test
  fun matchesExactHeader() {
    assertTrue(isSqliteHeader(magic))
  }

  @Test
  fun matchesHeaderWithTrailingContent() {
    assertTrue(isSqliteHeader(magic + byteArrayOf(0x10, 0x00, 0x01, 0x01)))
  }

  @Test
  fun rejectsTruncatedHeader() {
    assertFalse(isSqliteHeader(magic.copyOf(15)))
  }

  @Test
  fun rejectsMissingNulTerminator() {
    assertFalse(isSqliteHeader("SQLite format 3!".encodeToByteArray()))
  }

  @Test
  fun rejectsEmptyFile() {
    assertFalse(isSqliteHeader(ByteArray(0)))
  }

  @Test
  fun rejectsWalFile() {
    val walMagic = byteArrayOf(0x37, 0x7f, 0x06, 0x82.toByte()) + ByteArray(12)
    assertFalse(isSqliteHeader(walMagic))
  }

  @Test
  fun rejectsPlainText() {
    assertFalse(isSqliteHeader("this is not a database!!".encodeToByteArray()))
  }
}
