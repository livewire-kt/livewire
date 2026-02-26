package com.r0adkll.livewire.plugin.playground

import androidx.compose.runtime.Composable
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.ButtonStyle
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.IconButtonStyle
import com.r0adkll.livewire.ui.widget.Text

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

    }
  }
}
