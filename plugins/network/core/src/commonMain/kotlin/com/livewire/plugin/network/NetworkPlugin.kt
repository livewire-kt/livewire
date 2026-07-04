package com.livewire.plugin.network

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.livewire.plugin.network.composables.NetworkToolbar
import com.livewire.plugin.network.composables.RequestDetailPane
import com.livewire.plugin.network.composables.RequestListItem
import com.livewire.plugin.network.ui.Icons
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.animateContentSize
import com.livewire.ui.modifier.fillMaxHeight
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.widget.AnimatedVisibility
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.ResizableSurface
import com.livewire.ui.widget.ResizeAnchor
import com.livewire.ui.widget.Spacer

class NetworkPlugin : Plugin {

  private val presenter = NetworkPresenter()

  override val info: PluginInfo = PluginInfo(
    pluginId = "network",
    iconData = Icons.Network,
    title = "Network",
  )

  @Composable
  override fun Content() {
    val state = presenter.present()

    Row(
      modifier = LivewireModifier.fillMaxSize(),
    ) {
      // Request list (left pane)
      Column(
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxHeight()
          .animateContentSize(),
      ) {
        NetworkToolbar(
          filterText = state.filterText,
          onFilterChange = valueChangeAction {
            state.eventSink(NetworkUiEvent.UpdateFilter(it))
          },
          onClearAll = clickAction {
            state.eventSink(NetworkUiEvent.ClearAll)
          },
          eventCount = state.events.size,
        )

        HorizontalDivider(LivewireModifier.fillMaxWidth())

        Column(
          modifier = LivewireModifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(),
        ) {
          Spacer(LivewireModifier.height(8.dp))
          state.events.forEach { event ->
            RequestListItem(
              event = event,
              isSelected = state.selectedEvent?.id == event.id,
              onClick = clickAction {
                state.eventSink(NetworkUiEvent.SelectEvent(event))
              },
            )
          }
          Spacer(LivewireModifier.height(8.dp))
        }
      }

      // Detail pane (right side)
      AnimatedVisibility(
        visible = state.selectedEvent != null,
        modifier = LivewireModifier.fillMaxHeight(),
      ) {
        ResizableSurface(
          anchor = ResizeAnchor.Start,
          initialSize = 400.dp,
          minSize = 200.dp,
          maxSize = 600.dp,
          shadowElevation = 2.dp,
          modifier = LivewireModifier
            .fillMaxHeight(),
        ) {
          state.selectedEvent?.let { event ->
            RequestDetailPane(
              event = event,
              selectedTab = state.selectedDetailTab,
              onTabSelected = {
                state.eventSink(NetworkUiEvent.SelectDetailTab(it))
              },
              onClose = clickAction {
                state.eventSink(NetworkUiEvent.ClearSelection)
              },
            )
          }
        }
      }
    }
  }
}
