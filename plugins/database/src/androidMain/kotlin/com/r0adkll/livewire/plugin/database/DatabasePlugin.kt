package com.r0adkll.livewire.plugin.database

import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.database.composables.DatabaseToolBar
import com.r0adkll.livewire.plugin.database.composables.TableList
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.checkedChangeAction
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.valueChangeAction
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.border
import com.r0adkll.livewire.ui.modifier.clickable
import com.r0adkll.livewire.ui.modifier.clip
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.thenIf
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.Checkbox
import com.r0adkll.livewire.ui.widget.DropdownMenu
import com.r0adkll.livewire.ui.widget.DropdownMenuItem
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.IconButtonStyle
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextField
import com.r0adkll.livewire.ui.widget.TextStyle
import com.r0adkll.livewire.ui.widget.ToggleButton
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

    LaunchedEffect(Unit) {
      refreshDatabases()
    }

    LaunchedEffect(selectedDatabase) {
      refreshTables()
    }

    LaunchedEffect(selectedTable) {
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

    Column(LivewireModifier.fillMaxSize()) {

      DatabaseToolBar(
        selectedDatabase = selectedDatabase,
        availableDatabases = availableDatabases,
        onDatabaseSelected = { selectedDatabase = it },
        actions = {
          IconButton(
            action = clickAction {
              scope.launch {
                refreshDatabases()
                refreshTables()
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
            Row(
              modifier = LivewireModifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              currentQueryResult?.columns?.forEach { column ->
                Text(
                  text = column,
                  fontWeight = FontWeight.Bold.weight,
                  modifier = LivewireModifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            currentQueryResult?.rows?.forEach { row ->
              Row(
                modifier = LivewireModifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              ) {
                currentQueryResult?.columns?.forEachIndexed { index, column ->
                  val columnValue = row.getOrNull(index) ?: "null"
                  Text(
                    text = columnValue,
                    modifier = LivewireModifier
                      .weight(1f)
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}


