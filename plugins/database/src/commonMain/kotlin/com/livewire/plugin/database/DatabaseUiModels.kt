package com.livewire.plugin.database

import androidx.compose.runtime.Immutable
import com.livewire.plugin.database.data.DatabaseInfo
import com.livewire.plugin.database.data.QueryResult
import com.livewire.plugin.database.data.TableInfo

@Immutable
data class DatabaseUiState(
  val availableDatabases: List<DatabaseInfo>,
  val selectedDatabase: DatabaseInfo?,
  val selectedDatabaseTables: List<TableInfo>,
  val selectedTable: TableInfo?,
  val pages: List<DatabaseUiPage>,
  val eventSink: (DatabaseUiEvent) -> Unit,
)

sealed interface DatabaseUiEvent {
  data object Refresh : DatabaseUiEvent
  data class SelectDatabase(val database: DatabaseInfo) : DatabaseUiEvent
  data class SelectTable(val table: TableInfo) : DatabaseUiEvent

  data object AddQueryTab : DatabaseUiEvent
  data class RemoveQueryTab(val index: Int) : DatabaseUiEvent

  data class UpdateQueryForTab(val index: Int, val query: String) : DatabaseUiEvent
  data class ExecuteQueryForTab(val index: Int) : DatabaseUiEvent
}

sealed interface DatabaseUiPage {
  val name: String
  val closeable: Boolean get() = false
}

@Immutable
data class TableContentPage(
  val content: QueryResult? = null,
) : DatabaseUiPage {
  override val name: String
    get() = "Content"
}

@Immutable
data class QueryPage(
  override val name: String,
  val query: String = "",
  val result: QueryResult? = null,
) : DatabaseUiPage {
  override val closeable: Boolean = true
}
