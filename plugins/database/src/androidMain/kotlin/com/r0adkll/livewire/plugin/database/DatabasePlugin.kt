package com.r0adkll.livewire.plugin.database

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle
import kotlinx.coroutines.launch

class DatabasePlugin(context: Context) : Plugin {

  val inspector = DatabaseInspector(context)

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    iconData = DatabaseIconSvgData,
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

          Button(
            text = "Refresh",
            action = clickAction {
              scope.launch {
                refreshDatabases()
              }
            },
            modifier = LivewireModifier
              .padding(horizontal = 16.dp),
          )
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
          .weight(3f)
          .fillMaxHeight()
      ) {
        Text("CONTENT", LivewireModifier.padding(16.dp))
      }
    }
  }
}

private const val DatabaseIconSvgData = "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"24px\" viewBox=\"0 -960 960 960\" width=\"24px\" fill=\"#e8eaed\"><path d=\"M735-567q105-47 105-113T735-793q-105-47-255-47t-255 47q-105 47-105 113t105 113q105 47 255 47t255-47ZM582.5-428.5Q644-437 701-456t98-49.5q41-30.5 41-74.5v100q0 44-41 74.5T701-356q-57 19-118.5 27.5T480-320q-41 0-102.5-8.5T259-356q-57-19-98-49.5T120-480v-100q0 44 41 74.5t98 49.5q57 19 118.5 27.5T480-420q41 0 102.5-8.5Zm0 200Q644-237 701-256t98-49.5q41-30.5 41-74.5v100q0 44-41 74.5T701-156q-57 19-118.5 27.5T480-120q-41 0-102.5-8.5T259-156q-57-19-98-49.5T120-280v-100q0 44 41 74.5t98 49.5q57 19 118.5 27.5T480-220q41 0 102.5-8.5Z\"/></svg>"
