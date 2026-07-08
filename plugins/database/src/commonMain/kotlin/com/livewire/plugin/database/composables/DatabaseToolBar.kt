package com.livewire.plugin.database.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.livewire.plugin.database.data.DatabaseInfo
import com.livewire.plugin.database.ui.Database
import com.livewire.plugin.database.ui.DropdownArrow
import com.livewire.plugin.database.ui.Icons
import com.livewire.ui.actions.clickAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.BoxScope
import com.livewire.ui.layout.Row
import com.livewire.ui.layout.RowScope
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.width
import com.livewire.ui.widget.DropdownMenu
import com.livewire.ui.widget.DropdownMenuItem
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextStyle

@Composable
fun DatabaseToolBar(
  selectedDatabase: DatabaseInfo?,
  availableDatabases: List<DatabaseInfo>,
  onDatabaseSelected: (DatabaseInfo) -> Unit,
  actions: @Composable RowScope.() -> Unit = {},
  tabs: @Composable BoxScope.() -> Unit = {},
  modifier: LivewireModifier = LivewireModifier,
) {
  Surface(
    modifier = modifier
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
        tonalElevation = 2.dp,
        onClick = clickAction {
          menuExpanded = true
        },
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = LivewireModifier.padding(horizontal = 12.dp)
        ) {
          Text(
            text = selectedDatabase?.name ?: "<no database>",
            style = TextStyle.TitleMedium,
            modifier = LivewireModifier
              .padding(
                vertical = 6.dp,
              )
          )
          Spacer(LivewireModifier.width(8.dp))
          Icon(
            imageVector = Icons.DropdownArrow,
            modifier = LivewireModifier
              .size(24.dp)
          )
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = clickAction {
            menuExpanded = false
          },
        ) {
          availableDatabases.forEach { databaseInfo ->
            DropdownMenuItem(
              leadingIcon = Icons.Database,
              text = databaseInfo.name,
              onClick = clickAction {
                onDatabaseSelected(databaseInfo)
                menuExpanded = false
              },
            )
          }
        }
      }

      Spacer(LivewireModifier.width(16.dp))

      Box(
        modifier = LivewireModifier.weight(1f)
      ) {
        tabs()
      }

      Spacer(LivewireModifier.width(16.dp))

      actions()
    }
  }
}
