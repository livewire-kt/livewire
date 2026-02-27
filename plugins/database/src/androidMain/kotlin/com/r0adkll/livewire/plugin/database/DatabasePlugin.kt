package com.r0adkll.livewire.plugin.database

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.r0adkll.livewire.plugin.database.composables.DatabaseToolBar
import com.r0adkll.livewire.plugin.database.composables.TableList
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.intValueChangeAction
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.ButtonStyle
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.Tab
import com.r0adkll.livewire.ui.widget.TabRow
import com.r0adkll.livewire.ui.widget.Table
import com.r0adkll.livewire.ui.widget.Text
import kotlinx.coroutines.launch

class DatabasePlugin(context: Context) : Plugin {

  val inspector = DatabaseInspector(context)

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    iconData = Icons.Database,
    title = "Database",
  )

  @Composable
  override fun Content() {
    val scope = rememberCoroutineScope()
    val availableDatabases = remember { mutableStateListOf<DatabaseInfo>() }
    var selectedDatabase by remember { mutableStateOf<DatabaseInfo?>(null) }
    var selectedTable by remember { mutableStateOf<TableInfo?>(null) }

    val selectedTables = remember { mutableStateListOf<TableInfo>() }
    var currentQueryResult by remember { mutableStateOf<QueryResult?>(null) }

    suspend fun refreshDatabases() {
      Log.d("DatabasePlugin", "Refreshing Databases")
      inspector.discoverDatabases()
        .onSuccess { databases ->
          availableDatabases.clear()
          availableDatabases.addAll(databases)

          if (selectedDatabase == null) {
            selectedDatabase = databases.firstOrNull()
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
            selectedTables.clear()
            selectedTables.addAll(tables)
          }
          .onFailure { e ->
            e.printStackTrace()
          }
      }
    }

    suspend fun queryTable() {
      if (selectedDatabase != null && selectedTable != null) {
        inspector.getTableContents(selectedDatabase!!.path, selectedTable!!.name)
          .onSuccess { result ->
            currentQueryResult = result
          }
          .onFailure { e ->
            e.printStackTrace()
          }
      }
    }

    LaunchedEffect(Unit) {
      refreshDatabases()
    }

    LaunchedEffect(selectedDatabase) {
      refreshTables()
    }

    LaunchedEffect(selectedTable) {
      queryTable()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var extraQueryTabs by remember { mutableIntStateOf(0) }

    Column(LivewireModifier.fillMaxSize()) {
      DatabaseToolBar(
        selectedDatabase = selectedDatabase,
        availableDatabases = availableDatabases,
        onDatabaseSelected = { selectedDatabase = it },
        tabs = {
          TabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = intValueChangeAction {
              selectedTabIndex = it
            },
            modifier = LivewireModifier.fillMaxWidth(),
          ) {
            Tab(
              text = "Contents"
            )

            repeat(extraQueryTabs) { index ->
              Tab(
                text = "Query #$index",
              )
            }

            Button(
              action = clickAction {
                extraQueryTabs++
              },
              size = ButtonSize.ExtraSmall,
              style = ButtonStyle.Tonal,
            ) {
              Icon(Icons.DatabaseSearch)
              Text("New Query")
            }
          }
        },
        actions = {
          IconButton(
            action = clickAction {
              scope.launch {
                refreshDatabases()
                refreshTables()
                queryTable()
              }
            }
          ) {
            Icon(Icons.Sync)
          }
        }
      )

      Row {
        TableList(
          selected = selectedTable,
          tables = selectedTables,
          onTableClick = { selectedTable = it },
          modifier = LivewireModifier
            .weight(2f)
            .fillMaxHeight()
        )

        Column(
          modifier = LivewireModifier
            .weight(8f)
            .fillMaxHeight()
        ) {
          if (currentQueryResult != null) {
            Table(
              columns = currentQueryResult!!.columns,
              rows = currentQueryResult!!.rows.map { it.filterNotNull() },
              pageSize = 25,
              modifier = LivewireModifier.fillMaxSize(),
            )
          }
        }
      }
    }
  }
}


