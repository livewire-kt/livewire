package com.r0adkll.livewire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.r0adkll.livewire.com.r0adkll.livewire.theme.LivewireTheme
import com.r0adkll.livewire.plugin.host.ChatHostPlugin
import com.r0adkll.livewire.protocol.SimpleMessage
import com.r0adkll.livewire.runtime.AdbDevice
import com.r0adkll.livewire.runtime.AdbDeviceManager
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.HostConnectionState.CONNECTED
import com.r0adkll.livewire.runtime.HostConnectionState.CONNECTING
import com.r0adkll.livewire.runtime.HostConnectionState.DISCONNECTED
import com.r0adkll.livewire.runtime.HostConnectionState.ERROR
import com.r0adkll.livewire.runtime.HostConnectionState.FORWARDING
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.ui.layout.HostScaffold
import com.r0adkll.livewire.ui.plugin.PluginContent
import kotlinx.coroutines.launch

fun main() = application {
  val host = remember {
    LivewireHost {
      // TODO: Do we need manual installation, Could we use a java service locator or other-such to gather these
      install(ChatHostPlugin())
    }
  }

  val scope = rememberCoroutineScope()
  val state by host.connection.connectionState.collectAsState()
  val messages = remember { mutableStateListOf<String>() }

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

  // Collect incoming messages
  LaunchedEffect(Unit) {
    host.connection.incomingMessages.collect { envelope ->
      messages.add("$envelope")
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
    var selectedPlugin by remember { mutableStateOf<HostPlugin<*, *>?>(null) }

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
            onPingClick = {
              scope.launch {
                host.connection.send(SimpleMessage("Ping"))
              }
            },
            onClearClick = {
              selectedPlugin = null
            }
          )
        },
        drawer = {
          Column {
            host.configuration.plugins.forEach {
              PluginNavItem(
                plugin = it,
                onClick = {
                  selectedPlugin = it
                },
              )
            }
          }

          Text("Messages:", style = MaterialTheme.typography.titleSmall)
          LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(messages) { msg ->
              Text(msg, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      ) {
        selectedPlugin?.let { plugin ->
          PluginContent(
            plugin = plugin,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PluginNavItem(
  plugin: HostPlugin<*, *>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val presentation = plugin.createPresentation()
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Icon(
      presentation.icon,
      contentDescription = presentation.title,
    )

    Text(
      text = presentation.title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
    )
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
  onPingClick: () -> Unit,
  onClearClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var dropdownExpanded by remember { mutableStateOf(false) }
  Surface(
    modifier = modifier.fillMaxWidth(),
    shadowElevation = 2.dp,
    tonalElevation = 1.dp,
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        Modifier
          .padding(16.dp)
          .size(40.dp)
          .background(
            color = when (hostConnectionState) {
              DISCONNECTED -> Color.Gray
              FORWARDING -> Color.Blue
              CONNECTING -> Color.Yellow
              CONNECTED -> Color.Green
              ERROR -> Color.Red
            },
            shape = CircleShape,
          )
          .border(
            width = 2.dp,
            color = Color.Black,
            shape = CircleShape,
          )
      )

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

      Button(onClick = onRefreshClick) {
        Text("Refresh")
      }

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
          onClick = onPingClick,
          enabled = hostConnectionState == HostConnectionState.CONNECTED,
        ) {
          Text("Send Ping")
        }
        Button(
          onClick = onDisconnectClick,
          enabled = hostConnectionState == HostConnectionState.CONNECTED,
        ) {
          Text("Disconnect")
        }
        Button(
          onClick = onClearClick,
          enabled = hostConnectionState == HostConnectionState.CONNECTED,
        ) {
          Text("Clear")
        }
      }
    }
  }
}
