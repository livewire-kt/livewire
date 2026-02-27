package com.r0adkll.livewire.plugin.database.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.database.DatabaseInfo
import com.r0adkll.livewire.plugin.database.Icons
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.widget.DropdownMenu
import com.r0adkll.livewire.ui.widget.DropdownMenuItem
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle

@Composable
fun DatabaseToolBar(
  selectedDatabase: DatabaseInfo?,
  availableDatabases: List<DatabaseInfo>,
  onDatabaseSelected: (DatabaseInfo) -> Unit,
  actions: @Composable RowScope.() -> Unit = {},
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
        tonalElevation = 2f,
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
            svgData = Icons.DropdownArrow,
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
              leadingIconData = Icons.Database,
              text = databaseInfo.name,
              onClick = clickAction {
                onDatabaseSelected(databaseInfo)
                menuExpanded = false
              },
            )
          }
        }
      }

      Spacer(LivewireModifier.weight(1f))

      actions()
    }
  }
}
