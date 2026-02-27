package com.r0adkll.livewire.plugin.database.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.database.Icons
import com.r0adkll.livewire.plugin.database.TableInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.clickable
import com.r0adkll.livewire.ui.modifier.clip
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.thenIf
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle


@Composable
internal fun TableList(
  selected: TableInfo?,
  tables: List<TableInfo>,
  onTableClick: (TableInfo) -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) {
  Surface(
    modifier = modifier,
  ) {
    Column(
      modifier = LivewireModifier.fillMaxHeight()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = LivewireModifier
          .background(LivewireTheme.colorScheme.surfaceContainer)
          .height(48.dp)
          .fillMaxWidth()
      ) {
        Text(
          text = "Tables",
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
        tables.forEach { table ->
          val isSelected = selected == table

          Row(
            modifier = LivewireModifier
              .padding(
                horizontal = 8.dp,
                vertical = 2.dp,
              )
              .clip(CircleShape)
              .thenIf(isSelected) {
                background(LivewireTheme.colorScheme.primaryContainer)
              }
              .clickable(action = clickAction(table) {
                 onTableClick(table)
              })
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val contentColor = if (isSelected) {
              LivewireTheme.colorScheme.onPrimaryContainer
            } else {
              LivewireTheme.colorScheme.onSurface
            }

            Icon(
              svgData = Icons.Table,
              tint = contentColor,
              modifier = LivewireModifier.size(24.dp)
            )
            Spacer(LivewireModifier.width(16.dp))
            Text(
              text = table.name,
              color = contentColor,
              style = TextStyle.LabelLarge,
              fontWeight = if (isSelected) FontWeight.Bold.weight else null,
              modifier = LivewireModifier.weight(1f),
            )
          }
        }
      }
    }
  }
}
