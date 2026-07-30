package com.livewire.plugin.database.data

import java.io.File
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class JvmDatabaseInspectorTest {

  private val root = File.createTempFile("livewire-db-test", "").apply {
    delete()
    mkdirs()
  }

  @AfterTest
  fun tearDown() {
    root.deleteRecursively()
  }

  @Test
  fun discoversSqliteFilesRegardlessOfExtension() {
    createSqliteDatabase(File(root, "legacy.db"))
    createSqliteDatabase(File(root, "notes.sqlite"))
    createSqliteDatabase(File(root, "app_data"))
    File(root, "fake.db").writeText("just some text")
    File(root, "readme.txt").writeText("not a database")
    File(root, "legacy.db-journal").writeBytes(ByteArray(16))

    val discovered = runBlocking { JvmDatabaseInspector(listOf(root)).discoverDatabases() }
    val names = discovered.getOrThrow().map { it.name }.sorted()

    assertEquals(listOf("app_data", "legacy.db", "notes.sqlite"), names)
  }

  private fun createSqliteDatabase(file: File) {
    DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { connection ->
      connection.createStatement().use { it.execute("CREATE TABLE t (id INTEGER)") }
    }
  }
}
