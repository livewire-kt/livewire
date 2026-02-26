package com.r0adkll.livewire.plugin.playground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.checkedChangeAction
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.valueChangeAction
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.ButtonStyle
import com.r0adkll.livewire.ui.widget.Checkbox
import com.r0adkll.livewire.ui.widget.DropdownMenu
import com.r0adkll.livewire.ui.widget.DropdownMenuItem
import com.r0adkll.livewire.ui.widget.FabSize
import com.r0adkll.livewire.ui.widget.FloatingActionButton
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.IconButtonStyle
import com.r0adkll.livewire.ui.widget.ProgressIndicator
import com.r0adkll.livewire.ui.widget.ProgressIndicatorStyle
import com.r0adkll.livewire.ui.widget.RadioButton
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextField
import com.r0adkll.livewire.ui.widget.TextFieldStyle
import com.r0adkll.livewire.ui.widget.Switch
import com.r0adkll.livewire.ui.widget.ToggleButton

class PlaygroundPlugin : Plugin {
  override val info: PluginInfo = PluginInfo(
    pluginId = "playground",
    iconData = Icons.Playground,
    title = "Playground",
  )

  @Composable
  override fun Content() {
    Column(LivewireModifier.fillMaxSize()) {

      // Buttons
      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        Button(
          action = clickAction {

          }
        ) {
          Text("Filled")
        }

        Button(
          action = clickAction {

          },
          style = ButtonStyle.Tonal,
        ) {
          Text("Tonal")
        }

        Button(
          action = clickAction {

          },
          style = ButtonStyle.Outlined,
        ) {
          Text("Outlined")
        }

        Button(
          action = clickAction {

          },
          style = ButtonStyle.Elevated,
        ) {
          Text("Elevated")
        }

        Button(
          action = clickAction {

          },
          style = ButtonStyle.Text,
        ) {
          Text("Text")
        }

        IconButton(
          action = clickAction {

          },
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction {

          },
          style = IconButtonStyle.Filled
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction {

          },
          style = IconButtonStyle.Tonal
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction {

          },
          style = IconButtonStyle.Outlined
        ) { Icon(Icons.Sync) }

        var checked by remember { mutableStateOf(false) }
        ToggleButton(
          checked = checked,
          onCheckedChange = checkedChangeAction {
            checked = it
          }
        ) {
          Icon(Icons.Sync)
          Text(
            if (checked) "On" else "Off",
          )
        }
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          action = clickAction {

          },
          size = ButtonSize.Large,
        ) {
          Icon(Icons.Sync)
          Text("Large")
        }
        Button(
          action = clickAction {

          },
          size = ButtonSize.Medium,
        ) {
          Icon(Icons.Sync)
          Text("Medium")
        }
        Button(
          action = clickAction {

          },
          size = ButtonSize.Small,
        ) {
          Icon(Icons.Sync)
          Text("Small")
        }
        Button(
          action = clickAction {

          },
          size = ButtonSize.ExtraSmall,
        ) {
          Icon(Icons.Sync)
          Text("X-Small")
        }
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        FloatingActionButton(
          action = clickAction { },
          size = FabSize.Small,
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          Icon(Icons.Sync)
        }

        FloatingActionButton(
          action = clickAction { },
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          Icon(Icons.Sync)
        }

        FloatingActionButton(
          action = clickAction { },
          size = FabSize.Large,
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          Icon(Icons.Sync)
        }

        FloatingActionButton(
          action = clickAction { },
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          Icon(Icons.Sync)
          Text("Extended")
        }
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        var checked by remember { mutableStateOf(false) }
        Checkbox(
          checked = checked,
          onCheckedChange = checkedChangeAction {
            checked = it
          }
        )

        RadioButton(
          selected = checked,
          onClick = clickAction {
            checked = !checked
          }
        )

        var switchChecked by remember { mutableStateOf(true) }
        Switch(
          checked = switchChecked,
          onCheckedChange = checkedChangeAction {
            switchChecked = it
          },
        )

        Switch(
          checked = false,
          onCheckedChange = checkedChangeAction { },
          enabled = false,
        )
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        ProgressIndicator(
          modifier = LivewireModifier
            .width(200.dp)
            .padding(horizontal = 16.dp),
        )

        ProgressIndicator(
          progress = 0.5f,
          modifier = LivewireModifier
            .width(200.dp)
            .padding(horizontal = 16.dp),
        )

        ProgressIndicator(
          style = ProgressIndicatorStyle.Circular,
          modifier = LivewireModifier
            .padding(horizontal = 16.dp),
        )

        ProgressIndicator(
          progress = 0.5f,
          style = ProgressIndicatorStyle.Circular,
          modifier = LivewireModifier
            .padding(horizontal = 16.dp),
        )
      }


      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        TextField(
          initialValue = "",
          onValueChange = valueChangeAction {  },
          modifier = LivewireModifier
            .weight(1f)
            .padding(16.dp)
        )

        TextField(
          initialValue = "",
          onValueChange = valueChangeAction {  },
          style = TextFieldStyle.Outlined,
          modifier = LivewireModifier
            .weight(1f)
            .padding(16.dp)
        )

      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
        ) {
          Text("Flat")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(16.dp),
          tonalElevation = 2f,
        ) {
          Text("Rounded")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(24.dp),
          shadowElevation = 4f,
        ) {
          Text("Shadow")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = CircleShape,
          tonalElevation = 2f,
        ) {
          Text("Circle")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(12.dp),
          tonalElevation = 1f,
          onClick = clickAction { },
        ) {
          Text("Click")
        }
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        var menuExpanded by remember { mutableStateOf(false) }

        Box {
          Button(
            action = clickAction {
              menuExpanded = true
            },
          ) {
            Text("Show Menu")
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = clickAction {
              menuExpanded = false
            },
          ) {
            DropdownMenuItem(
              text = "Option 1",
              onClick = clickAction { menuExpanded = false },
              leadingIconData = Icons.Sync,
            )
            DropdownMenuItem(
              text = "Option 2",
              onClick = clickAction { menuExpanded = false },
            )
            DropdownMenuItem(
              text = "Disabled",
              onClick = clickAction { },
              enabled = false,
            )
          }
        }
      }

    }
  }
}
