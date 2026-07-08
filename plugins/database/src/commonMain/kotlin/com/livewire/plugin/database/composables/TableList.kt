package com.livewire.plugin.database.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.livewire.plugin.database.data.ColumnInfo
import com.livewire.plugin.database.ui.DropdownArrow
import com.livewire.plugin.database.ui.Icons
import com.livewire.plugin.database.ui.Table
import com.livewire.plugin.database.data.TableInfo
import com.livewire.ui.actions.clickAction
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.background
import com.livewire.ui.modifier.clickable
import com.livewire.ui.modifier.clip
import com.livewire.ui.modifier.fillMaxHeight
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextStyle


@Composable
internal fun TableList(
  tables: List<TableInfo>,
  onTableClick: (TableInfo) -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  Surface(
    modifier = modifier,
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = LivewireModifier.fillMaxHeight()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = LivewireModifier
          .background(LivewireTheme.colorScheme.surfaceContainer)
          .height(56.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = "Schema",
          style = TextStyle.TitleMedium,
          color = LivewireTheme.colorScheme.onSurface,
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
        val expandedTables = remember { mutableStateSetOf<TableInfo>() }

        tables.forEach { table ->
          val isExpanded = expandedTables.contains(table)
          TableRootListItem(
            name = table.name,
            onClick = { onTableClick(table) },
            action = {
              IconButton(
                action = clickAction(table.name + "_collapse") {
                  if (isExpanded) {
                    expandedTables.remove(table)
                  } else {
                    expandedTables.add(table)
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.DropdownArrow,
                )
              }
            }
          )

          if (isExpanded) {
            table.columns
              .sortedBy { it.index }
              .forEach { column ->
                ColumnListItem(column)
              }
          }
        }
      }
    }
  }
}

@Composable
private fun TableRootListItem(
  name: String,
  onClick: () -> Unit,
  action: @Composable () -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  Row(
    modifier = modifier
      .padding(
        horizontal = 6.dp,
        vertical = 2.dp,
      )
  ) {
    Row(
      modifier = modifier
        .clip(CircleShape)
        .clickable(action = clickAction(name) {
          onClick()
        })
        .weight(1f)
        .padding(horizontal = 8.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val contentColor = LivewireTheme.colorScheme.onSurface
      Icon(
        imageVector = Icons.Table,
        tint = contentColor,
        modifier = LivewireModifier.size(24.dp)
      )
      Spacer(LivewireModifier.width(8.dp))
      Text(
        text = name,
        color = contentColor,
        style = TextStyle.LabelLarge,
        fontWeight = FontWeight.Bold.weight,
        modifier = LivewireModifier.weight(1f),
      )
    }
    action()
  }
}

@Composable
private fun ColumnListItem(
  column: ColumnInfo,
  modifier: LivewireModifier = LivewireModifier,
) {
  Row(
    modifier = modifier
      .padding(
        horizontal = 6.dp,
        vertical = 2.dp,
      )
      .fillMaxWidth()
      .padding(horizontal = 10.dp),
  ) {
    Text(
      text = column.name,
      style = TextStyle.LabelSmall,
    )
    Spacer(LivewireModifier.width(2.dp))
    Text(
      text = column.type,
      style = TextStyle.LabelSmall,
      fontWeight = FontWeight.SemiBold.weight,
      color = LivewireTheme.colorScheme.primary,
    )
    if (column.notNull) {
      Spacer(LivewireModifier.width(2.dp))
      Text(
        text = "NOT NULL",
        style = TextStyle.LabelSmall,
        fontWeight = FontWeight.SemiBold.weight,
        color = LivewireTheme.colorScheme.primary,
      )
    }
    column.defaultValue?.let { defaultValue ->
      Spacer(LivewireModifier.width(2.dp))
      Text(
        text = "DEFAULT $defaultValue",
        style = TextStyle.LabelSmall,
        fontWeight = FontWeight.SemiBold.weight,
        color = LivewireTheme.colorScheme.primary,
      )
    }
    if (column.primaryKey) {
      Spacer(LivewireModifier.width(2.dp))
      Text(
        text = "PRIMARY KEY",
        style = TextStyle.LabelSmall,
        fontWeight = FontWeight.SemiBold.weight,
        color = LivewireTheme.colorScheme.primary,
      )
    }
  }
}
