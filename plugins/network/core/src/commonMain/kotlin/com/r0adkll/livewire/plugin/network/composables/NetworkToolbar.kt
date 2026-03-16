package com.r0adkll.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.network.ui.Icons
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.actions.ValueChangeAction
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextField
import com.r0adkll.livewire.ui.widget.TextFieldStyle

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
      Icon(svgData = Icons.Delete)
      Text("Clear")
    }

    Spacer(LivewireModifier.width(16.dp))
  }
}
