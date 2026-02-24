package com.r0adkll.livewire.plugin.database

data class DatabaseInfo(
  val name: String,
  val path: String,
  val sizeBytes: Long,
)

data class TableInfo(
  val name: String,
  val type: String,
  val columns: List<ColumnInfo>,
)

data class ColumnInfo(
  val index: Int,
  val name: String,
  val type: String,
  val notNull: Boolean,
  val defaultValue: String?,
  val primaryKey: Boolean,
)

data class QueryResult(
  val columns: List<String>,
  val rows: List<List<String?>>,
  val rowCount: Int,
  val executionTimeMs: Long,
)
