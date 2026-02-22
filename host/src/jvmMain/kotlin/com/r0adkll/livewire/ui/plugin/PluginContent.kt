package com.r0adkll.livewire.ui.plugin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.HostPlugin
import com.r0adkll.livewire.ui.LocalLivewireHost
import kotlinx.coroutines.launch

@Composable
internal fun PluginContent(
  plugin: HostPlugin<*, *>,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val host = LocalLivewireHost.current

  // Manage the DataConnection
  DisposableEffect(plugin) {
    // Start DataConnector on the client side
    host.startDataConnection(plugin)
    plugin.clientSink = {
      scope.launch {
        host.send(plugin, it)
      }
    }

    onDispose {
      // Stop DataConnector on the client side
      host.stopDataConnection(plugin)
      plugin.clientSink = {}
    }
  }

  // For as long as this content is in composition ingest the messages for the HostEvent type
  // of this plugin into the plugin.
  LaunchedEffect(Unit) {
    host.ingestMessages(plugin)
  }

  plugin.Content(modifier)
}
