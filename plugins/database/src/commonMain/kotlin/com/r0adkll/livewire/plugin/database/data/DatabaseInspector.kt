package com.r0adkll.livewire.plugin.database.data

interface DatabaseInspector {
  suspend fun discoverDatabases(): Result<List<DatabaseInfo>>
  suspend fun getTables(databasePath: String): Result<List<TableInfo>>
  suspend fun executeQuery(databasePath: String, sql: String): Result<QueryResult>
  suspend fun getTableContents(
    databasePath: String,
    tableName: String,
    limit: Int = 500,
  ): Result<QueryResult>
}
