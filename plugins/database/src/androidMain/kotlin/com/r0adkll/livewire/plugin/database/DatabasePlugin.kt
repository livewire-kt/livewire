package com.r0adkll.livewire.plugin.database

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.checkedChangeAction
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.valueChangeAction
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.widget.Checkbox
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.IconButtonStyle
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

    suspend fun refreshDatabases() {
      Log.d("DatabasePlugin", "Refreshing Databases")
      inspector.discoverDatabases()
        .onSuccess { databases ->
          availableDatabases.clear()
          availableDatabases.addAll(databases)
        }
        .onFailure { e ->
          e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
      refreshDatabases()
    }

    Row {
      Column(
        modifier = LivewireModifier.weight(2f)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = LivewireModifier.height(48.dp)
        ) {
          Text(
            text = "Available Databases",
            style = TextStyle.TitleMedium,
            modifier = LivewireModifier
              .padding(horizontal = 16.dp)
          )

          IconButton(
            action = clickAction {
              scope.launch {
                refreshDatabases()
              }
            },
            style = IconButtonStyle.Tonal,
            modifier = LivewireModifier
              .padding(horizontal = 16.dp),
          ) {
            Icon(Icons.Sync)
          }

          var checked by remember { mutableStateOf(false) }
          Checkbox(
            checked = checked,
            onCheckedChange = checkedChangeAction {
              checked = it
            },
            modifier = LivewireModifier
              .padding(horizontal = 16.dp),
            enabled = true,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {

          var inputText by remember { mutableStateOf("") }
          TextField(
            initialValue = "",
            onValueChange = valueChangeAction {
              Log.d("DatabasePlugin", "Changing input: $it")
              inputText = it
            },
            modifier = LivewireModifier
              .weight(1f)
              .padding(16.dp),
          )

          var toggleButtonChecked by remember { mutableStateOf(false) }
          ToggleButton(
            checked = toggleButtonChecked,
            onCheckedChange = checkedChangeAction {
              toggleButtonChecked = it
            }
          ) {
            Text(if (toggleButtonChecked) "On" else "Off")
          }
        }

        availableDatabases.forEach { db ->
          Text(
            text = db.name,
            modifier = LivewireModifier
              .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
              ),
          )
        }
      }

      Box(
        modifier = LivewireModifier
          .weight(2f)
          .fillMaxHeight()
      ) {
        Text("CONTENT", LivewireModifier.padding(16.dp))
      }
    }
  }
}


