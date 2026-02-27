package com.r0adkll.livewire.plugin.database

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

class DatabasePresenter(
  private val inspector: DatabaseInspector,
) {

  private val availableDatabases = mutableStateListOf<DatabaseInfo>()
  private var selectedDatabase by mutableStateOf<DatabaseInfo?>(null)
  private var selectedDatabaseTables = mutableStateListOf<TableInfo>()
  private var selectedTable by mutableStateOf<TableInfo?>(null)

  private val pages = mutableStateListOf<DatabaseUiPage>(
    TableContentPage()
  )

  @Composable
  fun present(): DatabaseUiState {
    val scope = rememberCoroutineScope()

    // Load the list of databases initially
    LaunchedEffect(Unit) {
      refreshDatabases()
    }

    return DatabaseUiState(
      availableDatabases = availableDatabases,
      selectedDatabase = selectedDatabase,
      selectedDatabaseTables = selectedDatabaseTables,
      selectedTable = selectedTable,
      pages = pages,
    ) { event ->
      when (event) {
        DatabaseUiEvent.Refresh -> scope.launch {
          refreshDatabases()
          refreshTables()
          queryTable()
        }

        is DatabaseUiEvent.SelectDatabase -> {
          selectedDatabase = event.database
          scope.launch {
            refreshTables()
          }
        }

        DatabaseUiEvent.AddQueryTab -> {
          val nextIndex = pages.filterIsInstance<QueryPage>().size
          pages.addLast(
            QueryPage(
              name = "Query #$nextIndex",
            )
          )
        }

        is DatabaseUiEvent.RemoveQueryTab -> {
          pages.removeAt(event.index)
        }

        is DatabaseUiEvent.ExecuteQueryForTab -> scope.launch {
          refreshQueryResults(event.index)
        }

        is DatabaseUiEvent.SelectTable -> scope.launch {
          selectTable(event.table)
        }
        is DatabaseUiEvent.UpdateQueryForTab -> {
          val page = pages.getOrNull(event.index) as? QueryPage
          if (page != null) {
            pages[event.index] = page.copy(
              query = event.query,
            )
          }
        }
      }
    }
  }

  suspend fun selectTable(table: TableInfo) {
    selectedTable = table

    // Query content
    val content = inspector.getTableContents(selectedDatabase!!.path, selectedTable!!.name)
      .getOrNull()

    updateTableContentPage(content)
  }

  private fun updateTableContentPage(
    content: QueryResult?,
  ) {
    if (pages.isEmpty() || pages.first() !is TableContentPage) {
      pages.addFirst(
        TableContentPage(
          content = content,
        )
      )
    } else if (pages.first() is TableContentPage) {
      pages[0] = TableContentPage(
        content = content,
      )
    }
  }

  suspend fun refreshDatabases() {
    Log.d("DatabasePlugin", "Refreshing Databases")
    inspector.discoverDatabases()
      .onSuccess { databases ->
        availableDatabases.clear()
        availableDatabases.addAll(databases)

        if (selectedDatabase == null) {
          selectedDatabase = databases.firstOrNull()
          refreshTables()
        }
      }
      .onFailure { e ->
        e.printStackTrace()
      }
  }

  suspend fun refreshTables() {
    if (selectedDatabase != null) {
      inspector.getTables(selectedDatabase!!.path)
        .onSuccess { tables ->
          selectedDatabaseTables.clear()
          selectedDatabaseTables.addAll(tables)
        }
        .onFailure { e ->
          e.printStackTrace()
        }
    }
  }

  private suspend fun queryTable() {
    if (selectedDatabase != null && selectedTable != null) {
      inspector.getTableContents(selectedDatabase!!.path, selectedTable!!.name)
        .onSuccess { result ->
          updateTableContentPage(result)
        }
        .onFailure { e ->
          e.printStackTrace()
        }
    }
  }

  private suspend fun refreshQueryResults(index: Int) {
    val page = pages.getOrNull(index) as? QueryPage
    if (page != null && selectedDatabase != null) {
      inspector.executeQuery(
        databasePath = selectedDatabase!!.path,
        sql = page.query,
      ).onSuccess { result ->
        pages[index] = page.copy(
          result = result,
        )
      }.onFailure { e ->
        e.printStackTrace()
      }
    }
  }
}
