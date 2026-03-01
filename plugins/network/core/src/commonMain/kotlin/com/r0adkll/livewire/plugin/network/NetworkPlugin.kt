package com.r0adkll.livewire.plugin.network

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.network.composables.NetworkToolbar
import com.r0adkll.livewire.plugin.network.composables.RequestDetailPane
import com.r0adkll.livewire.plugin.network.composables.RequestListItem
import com.r0adkll.livewire.plugin.network.ui.Icons
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.intValueChangeAction
import com.r0adkll.livewire.ui.actions.valueChangeAction
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.widget.AnimatedVisibility
import com.r0adkll.livewire.ui.widget.HorizontalDivider
import com.r0adkll.livewire.ui.widget.Surface

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
          .fillMaxHeight(),
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
          state.events.forEach { event ->
            RequestListItem(
              event = event,
              isSelected = state.selectedEvent?.id == event.id,
              onClick = clickAction(event.id) {
                state.eventSink(NetworkUiEvent.SelectEvent(event))
              },
            )
            HorizontalDivider(LivewireModifier.fillMaxWidth())
          }
        }
      }

      // Detail pane (right side)
      AnimatedVisibility(
        visible = state.selectedEvent != null,
        modifier = LivewireModifier.fillMaxHeight(),
      ) {
        Surface(
          shadowElevation = 2f,
          modifier = LivewireModifier
            .fillMaxHeight()
            .width(400.dp),
        ) {
          state.selectedEvent?.let { event ->
            RequestDetailPane(
              event = event,
              selectedTab = state.selectedDetailTab,
              onTabSelected = intValueChangeAction {
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
