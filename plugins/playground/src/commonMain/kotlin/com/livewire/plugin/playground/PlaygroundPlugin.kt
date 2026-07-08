package com.livewire.plugin.playground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import com.livewire.ui.actions.checkedChangeAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.floatValueChangeAction
import com.livewire.ui.actions.intValueChangeAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.modifier.width
import com.livewire.ui.widget.AnimatedVisibility
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.Checkbox
import com.livewire.ui.widget.Chip
import com.livewire.ui.widget.ChipStyle
import com.livewire.ui.widget.DropdownMenu
import com.livewire.ui.widget.DropdownMenuItem
import com.livewire.ui.widget.FabSize
import com.livewire.ui.widget.FloatingActionButton
import com.livewire.ui.widget.FloatingToolbar
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.IconButtonStyle
import com.livewire.ui.widget.ProgressIndicator
import com.livewire.ui.widget.ProgressIndicatorStyle
import com.livewire.ui.widget.RadioButton
import com.livewire.ui.widget.Slider
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Switch
import com.livewire.ui.widget.Tab
import com.livewire.ui.widget.TabRow
import com.livewire.ui.widget.Table
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextField
import com.livewire.ui.widget.TextFieldStyle
import com.livewire.ui.widget.ToggleButton
import com.livewire.ui.widget.VerticalDivider

class PlaygroundPlugin : Plugin {
  override val info: PluginInfo = PluginInfo(
    pluginId = "playground",
    title = "Playground",
    icon = Icons.Playground,
  )

  @Composable
  override fun Content() {
    Column(
      LivewireModifier
        .fillMaxSize()
        .verticalScroll(),
    ) {

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        Button(
          action = clickAction { },
        ) {
          Text("Filled")
        }

        Button(
          action = clickAction { },
          style = ButtonStyle.Tonal,
        ) {
          Text("Tonal")
        }

        Button(
          action = clickAction { },
          style = ButtonStyle.Outlined,
        ) {
          Text("Outlined")
        }

        Button(
          action = clickAction { },
          style = ButtonStyle.Elevated,
        ) {
          Text("Elevated")
        }

        Button(
          action = clickAction { },
          style = ButtonStyle.Text,
        ) {
          Text("Text")
        }

        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction { },
          style = IconButtonStyle.Filled,
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction { },
          style = IconButtonStyle.Tonal,
        ) { Icon(Icons.Sync) }

        IconButton(
          action = clickAction { },
          style = IconButtonStyle.Outlined,
        ) { Icon(Icons.Sync) }

        var checked by remember { mutableStateOf(false) }
        ToggleButton(
          checked = checked,
          onCheckedChange = checkedChangeAction {
            checked = it
          },
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
          action = clickAction { },
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
          },
        )

        RadioButton(
          selected = checked,
          onClick = clickAction {
            checked = !checked
          },
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
          onValueChange = valueChangeAction { },
          modifier = LivewireModifier
            .weight(1f)
            .padding(16.dp),
        )

        TextField(
          initialValue = "",
          onValueChange = valueChangeAction { },
          style = TextFieldStyle.Outlined,
          modifier = LivewireModifier
            .weight(1f)
            .padding(16.dp),
        )
      }

      Row(
        LivewireModifier
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {

        var sliderValue by remember { mutableStateOf(0.5f) }
        Slider(
          value = sliderValue,
          onValueChange = floatValueChangeAction {
            sliderValue = it
          },
          modifier = LivewireModifier
            .weight(1f)
            .padding(horizontal = 16.dp),
        )

        var steppedValue by remember { mutableStateOf(0f) }
        Slider(
          value = steppedValue,
          onValueChange = floatValueChangeAction {
            steppedValue = it
          },
          steps = 4,
          modifier = LivewireModifier
            .weight(1f)
            .padding(horizontal = 16.dp),
        )

        Slider(
          value = 0.3f,
          onValueChange = floatValueChangeAction { },
          enabled = false,
          modifier = LivewireModifier
            .weight(1f)
            .padding(horizontal = 16.dp),
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
          tonalElevation = 2.dp,
        ) {
          Text("Rounded")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(24.dp),
          shadowElevation = 4.dp,
        ) {
          Text("Shadow")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = CircleShape,
          tonalElevation = 2.dp,
        ) {
          Text("Circle")
        }

        Surface(
          modifier = LivewireModifier
            .size(150.dp, 80.dp)
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(12.dp),
          tonalElevation = 1.dp,
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

        FloatingToolbar(
          expanded = true,
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
        }

        FloatingToolbar(
          expanded = false,
          modifier = LivewireModifier.padding(horizontal = 16.dp),
        ) {
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
          IconButton(
            action = clickAction { },
          ) { Icon(Icons.Sync) }
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
              leadingIcon = Icons.Sync,
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

      Row(
        LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Left")
        Spacer(modifier = LivewireModifier.width(48.dp))
        Text("Center")
        Spacer(modifier = LivewireModifier.width(48.dp))
        Text("Right")
      }

      HorizontalDivider(
        modifier = LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )

      Row(
        LivewireModifier
          .fillMaxWidth()
          .height(48.dp)
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Left")
        VerticalDivider(modifier = LivewireModifier.padding(horizontal = 16.dp))
        Text("Center")
        VerticalDivider(modifier = LivewireModifier.padding(horizontal = 16.dp))
        Text("Right")
      }

      HorizontalDivider(
        modifier = LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        thickness = 3.dp,
      )

      Column(
        modifier = LivewireModifier
          .height(200.dp)
          .verticalScroll()
          .padding(horizontal = 16.dp),
      ) {
        repeat(20) {
          Text("Item $it")
        }
      }

      Row(
        LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Chip(
          label = "Assist",
          action = clickAction { },
        )
        Chip(
          label = "Assist Icon",
          action = clickAction { },
          leadingIcon = Icons.Sync,
        )
        Chip(
          label = "Elevated",
          action = clickAction { },
          elevated = true,
        )

        var filterSelected by remember { mutableStateOf(false) }
        Chip(
          label = "Filter",
          action = clickAction { filterSelected = !filterSelected },
          style = ChipStyle.Filter,
          selected = filterSelected,
        )

        Chip(
          label = "Input",
          action = clickAction { },
          style = ChipStyle.Input,
          selected = true,
          leadingIcon = Icons.Sync,
        )

        Chip(
          label = "Suggestion",
          action = clickAction { },
          style = ChipStyle.Suggestion,
        )
      }

      var selectedTab by remember { mutableStateOf(0) }
      TabRow(
        selectedTabIndex = selectedTab,
        onTabSelected = intValueChangeAction {
          selectedTab = it
        },
        modifier = LivewireModifier.fillMaxWidth(),
      ) {
        Tab(text = "Overview")
        Tab(text = "Details")
        Tab(text = "Settings")
      }

      Row(
        LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        var contentVisible by remember { mutableStateOf(true) }
        Button(
          action = clickAction {
            contentVisible = !contentVisible
          },
        ) {
          Text(if (contentVisible) "Hide" else "Show")
        }

        AnimatedVisibility(visible = contentVisible) {
          Text("Hello, AnimatedVisibility!")
        }
      }

      Table(
        columns = listOf("Name", "Role", "Status"),
        rows = listOf(
          listOf("Alice", "Engineer", "Active"),
          listOf("Bob", "Designer", "Active"),
          listOf("Charlie", "PM", "Away"),
          listOf("Diana", "Engineer", "Active"),
          listOf("Eve", "QA", "Offline"),
          listOf("Frank", "DevOps", "Active"),
          listOf("Grace", "Designer", "Away"),
          listOf("Hank", "Engineer", "Active"),
          listOf("Iris", "PM", "Active"),
          listOf("Jack", "QA", "Offline"),
          listOf("Karen", "Engineer", "Away"),
          listOf("Leo", "Designer", "Active"),
          listOf("Mona", "DevOps", "Active"),
          listOf("Nate", "Engineer", "Offline"),
          listOf("Olivia", "PM", "Active"),
        ),
        pageSize = 5,
        modifier = LivewireModifier
          .fillMaxWidth()
          .height(300.dp)
          .padding(16.dp),
      )
    }
  }
}
