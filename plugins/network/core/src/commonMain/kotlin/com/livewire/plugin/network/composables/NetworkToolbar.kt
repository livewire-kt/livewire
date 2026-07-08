package com.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.livewire.plugin.network.ui.Delete
import com.livewire.plugin.network.ui.Icons
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.background
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextField
import com.livewire.ui.widget.TextFieldStyle

@Composable
internal fun NetworkToolbar(
  filterText: String,
  onFilterChange: ValueChangeAction,
  onClearAll: ClickAction,
  eventCount: Int,
  modifier: LivewireModifier = LivewireModifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .background(LivewireTheme.colorScheme.surfaceContainer)
      .fillMaxWidth(),
  ) {
    TextField(
      initialValue = filterText,
      onValueChange = onFilterChange,
      placeholder = "Filter by URL, method, or status…",
      singleLine = true,
      style = TextFieldStyle.Outlined,
      modifier = LivewireModifier
        .weight(1f)
        .padding(
          horizontal = 16.dp,
          vertical = 8.dp,
        ),
    )

    Button(
      action = onClearAll,
    ) {
      Icon(imageVector = Icons.Delete)
      Text("Clear")
    }

    Spacer(LivewireModifier.width(16.dp))
  }
}
