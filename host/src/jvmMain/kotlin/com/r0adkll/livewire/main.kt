package com.r0adkll.livewire

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.r0adkll.livewire.com.r0adkll.livewire.theme.LivewireTheme
import com.r0adkll.livewire.runtime.AdbDevice
import com.r0adkll.livewire.runtime.AdbDeviceManager
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.HostConnectionState.CONNECTED
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.ui.PluginDrawerItem
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.data.ClientManifest
import com.r0adkll.livewire.ui.data.PluginSelected
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.icons.Connected
import com.r0adkll.livewire.ui.icons.Disconnected
import com.r0adkll.livewire.ui.layout.HostScaffold
import com.r0adkll.livewire.ui.layout.LayoutNodeContent
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
fun main() = application {
  val host = remember { LivewireHost() }

  val scope = rememberCoroutineScope()
  val state by host.connection.connectionState.collectAsState()

  var devices by remember { mutableStateOf<List<AdbDevice>>(emptyList()) }
  var selectedDevice by remember { mutableStateOf<AdbDevice?>(null) }

  fun refreshDevices() {
    scope.launch {
      val result = AdbDeviceManager.listDevices()
      devices = result.getOrDefault(emptyList())
      // Auto-select first device if current selection is no longer available
      if (selectedDevice == null || selectedDevice !in devices) {
        selectedDevice = devices.firstOrNull()
      }
    }
  }

  // Scan devices on launch
  LaunchedEffect(Unit) {
    refreshDevices()
  }

  // Collect the current manifest
  var clientManifest by remember { mutableStateOf<ClientManifest?>(null) }
  var selectedPlugin by remember { mutableStateOf<PluginInfo?>(null) }

  // Always collect the client manifest regardless of connection state
  LaunchedEffect(Unit) {
    host.connection.incomingMessages
      .filterIsInstance<ClientManifest>()
      .collect { clientManifest = it }
  }

  // Clear the plugin if we ever become disconnected from the server
  LaunchedEffect(state) {
    if (state != CONNECTED) {
      selectedPlugin = null
      clientManifest = null
    }
  }

  Window(
    onCloseRequest = {
      host.connection.close()
      exitApplication()
    },
    title = "Livewire Host",
    state = rememberWindowState(
      size = DpSize(1200.dp, 800.dp),
    )
  ) {

    LivewireTheme(host) {
      HostScaffold(
        topBar = {
          DeviceTopBar(
            hostConnectionState = state,
            devices = devices,
            selectedDevice = selectedDevice,
            onDeviceClick = { selectedDevice = it },
            onRefreshClick = { refreshDevices() },
            onConnectClick = {
              host.connection.connect(it)
            },
            onDisconnectClick = { host.connection.disconnect() },
          )
        },
        drawer = {
          Column {
            Row(
              modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Plugins",
                style = MaterialTheme.typography.titleMediumEmphasized
              )
            }

            LazyColumn(
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(
                horizontal = 16.dp,
              ),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              clientManifest?.availablePlugins?.let { availablePlugins ->
                items(
                  items = availablePlugins.toList(),
                  key = { it.pluginId },
                ) { plugin ->
                  PluginDrawerItem(
                    selected = plugin == selectedPlugin,
                    info = plugin,
                    onClick = {
                      selectedPlugin = plugin
                      scope.launch {
                        val msg: UiProtocol = PluginSelected(plugin)
                        host.connection.send(msg)
                      }
                    },
                  )
                }
              }


            }
          }
        }
      ) {
        val layoutNode by host.connection.incomingLayoutNodes.collectAsState()

        CompositionLocalProvider(
          LocalLivewireActionDispatcher provides host,
        ) {
          LayoutNodeContent(
            node = layoutNode,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }
    }
  }
}

@Composable
private fun DeviceTopBar(
  hostConnectionState: HostConnectionState,
  devices: List<AdbDevice>,
  selectedDevice: AdbDevice?,
  onDeviceClick: (AdbDevice) -> Unit,
  onRefreshClick: () -> Unit,
  onConnectClick: (AdbDevice) -> Unit,
  onDisconnectClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var dropdownExpanded by remember { mutableStateOf(false) }
  Surface(
    modifier = modifier.fillMaxWidth(),
    shadowElevation = 2.dp,
    tonalElevation = 1.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(56.dp),
        contentAlignment = Alignment.Center,
      ) {
        val tint by animateColorAsState(
          if (hostConnectionState == CONNECTED) Color(0xff118F00) else MaterialTheme.colorScheme.error
        )
        Icon(
          if (hostConnectionState == CONNECTED) {
            Connected
          } else {
            Disconnected
          },
          contentDescription = null,
          tint = tint,
        )
      }

      Box {
        OutlinedButton(onClick = { dropdownExpanded = true }) {
          Text(
            selectedDevice?.displayName ?: "No devices",
          )
        }
        DropdownMenu(
          expanded = dropdownExpanded,
          onDismissRequest = { dropdownExpanded = false },
        ) {
          devices.forEach { device ->
            DropdownMenuItem(
              text = { Text(device.displayName) },
              onClick = {
                onDeviceClick(device)
                dropdownExpanded = false
              },
            )
          }
        }
      }

      Spacer(Modifier.width(8.dp))

      Button(onClick = onRefreshClick) {
        Text("Refresh")
      }

      Spacer(Modifier.width(8.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            selectedDevice?.let {
              onConnectClick(it)
            }
          },
          enabled = selectedDevice != null &&
            (hostConnectionState == HostConnectionState.DISCONNECTED || hostConnectionState == HostConnectionState.ERROR),
        ) {
          Text("Connect")
        }
        Button(
          onClick = onDisconnectClick,
          enabled = hostConnectionState == HostConnectionState.CONNECTED,
        ) {
          Text("Disconnect")
        }
      }
    }
  }
}
