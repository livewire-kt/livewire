package com.livewire.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DrawerContent(
  expanded: Boolean,
  selectedPlugin: PluginInfo?,
  availablePlugins: List<PluginInfo>,
  onPluginClick: (PluginInfo) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxHeight()
      .padding(vertical = 8.dp),
  ) {
    LazyColumn(
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(
        horizontal = 8.dp,
      ),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(
        items = availablePlugins,
        key = { it.pluginId },
      ) { plugin ->
        PluginDrawerItem(
          expanded = expanded,
          selected = plugin == selectedPlugin,
          info = plugin,
          onClick = {
            onPluginClick(plugin)
          },
        )
      }
    }
  }
}
