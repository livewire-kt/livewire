package com.livewire.plugin.network.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.livewire.plugin.network.ui.Delete
import com.livewire.plugin.network.ui.Icons
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.background
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.BasicTextField
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonShapes
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextField
import com.livewire.ui.widget.TextFieldStyle
import com.livewire.ui.widget.TextStyle

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
      .background(LivewireTheme.colorScheme.surfaceContainerHigh)
      .fillMaxWidth()
      .padding(horizontal = 8.dp),
  ) {
    Surface(
      modifier = LivewireModifier
        .weight(1f)
        .padding(
          vertical = 8.dp,
        ),
      shape = RoundedCornerShape(8.dp),
      tonalElevation = 2.dp,
    ) {
      BasicTextField(
        initialValue = filterText,
        onValueChange = onFilterChange,
        placeholder = "Filter by URL, method, or status…",
        singleLine = true,
        textStyle = TextStyle.BodyMedium,
        modifier = LivewireModifier
          .fillMaxWidth()
          .padding(12.dp),
      )
    }

    Spacer(LivewireModifier.width(8.dp))

    Button(
      action = onClearAll,
      size = ButtonSize.Small,
      style = ButtonStyle.Tonal,
      shapes = ButtonShapes(
        shape = RoundedCornerShape(8.dp),
        pressedShape = CircleShape,
      )
    ) {
      Icon(imageVector = Icons.Delete)
      Text("Clear")
    }
  }
}
