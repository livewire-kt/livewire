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
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.ui.modifier.width
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

    LaunchedEffect(Unit) {
      refreshDatabases()
    }

    LaunchedEffect(selectedDatabase) {
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

      Surface(
        modifier = LivewireModifier
          .fillMaxWidth()
          .height(56.dp),
      ) {

        Row(
          modifier = LivewireModifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          var menuExpanded by remember { mutableStateOf(false) }
          Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 2f,
            onClick = clickAction {
              menuExpanded = true
            },
          ) {
            Text(
              text = selectedDatabase?.name ?: "<no database>",
              style = TextStyle.TitleMedium,
              modifier = LivewireModifier
                .padding(
                  horizontal = 12.dp,
                  vertical = 6.dp,
                )
            )

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = clickAction {
                menuExpanded = false
              },
            ) {
              availableDatabases.forEach { databaseInfo ->
                DropdownMenuItem(
                  text = databaseInfo.name,
                  onClick = clickAction {
                    selectedDatabase = databaseInfo
                    menuExpanded = false
                  },
                )
              }
            }
          }
        }
      }

      Row {
        Surface(
          modifier = LivewireModifier
            .weight(2f)
            .fillMaxHeight()
            .border(2.dp, Color.Red),
          shadowElevation = 4f,
        ) {
          Column(
            modifier = LivewireModifier.fillMaxHeight()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = LivewireModifier
                .height(48.dp)
                .fillMaxWidth()
            ) {
              Text(
                text = "Tables",
                style = TextStyle.TitleMedium,
                modifier = LivewireModifier
                  .padding(horizontal = 16.dp)
              )
            }

            Column(
              modifier = LivewireModifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(),
            ) {
              selectedTables.forEach { table ->
                val isSelected = selectedTable == table

                Row(
                  modifier = LivewireModifier
                    .padding(
                      horizontal = 8.dp,
                      vertical = 2.dp,
                    )
                    .clip(CircleShape)
                    .clickable(action = clickAction(table) {
                      selectedTable = table
                    })
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    svgData = Icons.Table,
                    // TODO: Lets figure out theming and color tokens
                    tint = if (isSelected) Color.Blue else Color.Unspecified,
                    modifier = LivewireModifier.size(24.dp)
                  )
                  Spacer(LivewireModifier.width(16.dp))
                  Text(
                    text = table.name,
                    style = TextStyle.LabelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold.weight else null,
                    modifier = LivewireModifier.weight(1f),
                  )
                }
              }
            }
          }
        }

        Column(
          modifier = LivewireModifier
            .weight(8f)
            .fillMaxHeight()
            .border(2.dp, Color.Red)
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


