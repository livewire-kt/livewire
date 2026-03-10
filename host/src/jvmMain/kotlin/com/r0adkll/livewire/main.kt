package com.r0adkll.livewire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.HostConnectionState.Error
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.runtime.devicemanager.CompositeDeviceManager
import com.r0adkll.livewire.runtime.devicemanager.HostDevice
import com.r0adkll.livewire.theme.LivewireThemeContent
import com.r0adkll.livewire.ui.PluginDrawerItem
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.composables.DisconnectedStateLayout
import com.r0adkll.livewire.ui.data.ClientManifest
import com.r0adkll.livewire.ui.data.PluginSelected
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.host.DebugNodes
import com.r0adkll.livewire.ui.host.LayoutNodeContent
import com.r0adkll.livewire.ui.icons.BugReport
import com.r0adkll.livewire.ui.icons.ConnectedIcon
import com.r0adkll.livewire.ui.icons.DisconnectedIcon
import com.r0adkll.livewire.ui.icons.MenuOpen
import com.r0adkll.livewire.ui.layout.HostDrawerSheet
import com.r0adkll.livewire.ui.layout.HostScaffold
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
fun main() = application {
  val host = remember { LivewireHost() }
  LaunchedEffect(Unit) {
    Runtime.getRuntime().addShutdownHook(Thread {
      runBlocking {
        host.connection.close()
      }

      CompositeDeviceManager.shutdown()
    })
  }

  val scope = rememberCoroutineScope()
  val state by host.connection.connectionState.collectAsState()

  var devices by remember { mutableStateOf<List<HostDevice>>(emptyList()) }
  var selectedDevice by remember { mutableStateOf<HostDevice?>(null) }

  LaunchedEffect(Unit) {
    CompositeDeviceManager.deviceList().collect { deviceList ->
      devices = deviceList

      if (selectedDevice == null || deviceList.none { selectedDevice?.id == it.id }) {
        selectedDevice = deviceList.firstOrNull()
        // TODO: close connections with disconnected devices
      }
    }
  }

  var clientManifest by remember { mutableStateOf<ClientManifest?>(null) }
  var selectedPlugin by remember { mutableStateOf<PluginInfo?>(null) }

  LaunchedEffect(host.connection) {
    host.connection.incomingMessages
      .filterIsInstance<ClientManifest>()
      .collect {
        // TODO: There's gotta be a better way to do this.
        //  Switch out the layoutNode parsing based on what the client is reporting.
        host.connection.codec.serializationStrategy = it.layoutNodeSerialization.toStrategy()
        clientManifest = it
      }
  }

  LaunchedEffect(state) {
    if (state != Connected) {
      selectedPlugin = null
      clientManifest = null
    }
  }

  Window(
    onCloseRequest = { exitApplication() },
    title = "Livewire Host",
    state = rememberWindowState(
      size = DpSize(1200.dp, 800.dp),
    )
  ) {
    LivewireThemeContent(
      theme = clientManifest?.theme ?: LivewireTheme(),
      host = host
    ) {
      var menuExpanded by remember { mutableStateOf(true) }
      HostScaffold(
        topBar = {
          DeviceTopBar(
            hostConnectionState = state,
            devices = devices,
            selectedDevice = selectedDevice,
            onDeviceClick = { selectedDevice = it },
            onConnectClick = { device ->
              scope.launch { host.connection.connect(device) }
            },
            onDisconnectClick = {
              scope.launch { host.connection.disconnect() }
            },
            onNavigationItemClick = {
              menuExpanded = !menuExpanded
            }
          )
        },
        drawer = {
          AnimatedVisibility(
            visible = clientManifest != null,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it },
          ) {
            HostDrawerSheet {
              DrawerContent(
                expanded = menuExpanded,
                selectedPlugin = selectedPlugin,
                availablePlugins = clientManifest?.availablePlugins?.toList() ?: emptyList(),
                onPluginClick = { plugin ->
                  selectedPlugin = plugin
                  scope.launch {
                    val msg: UiProtocol = PluginSelected(plugin)
                    host.connection.send(msg)
                  }
                }
              )
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

        if (state != Connected) {
          DisconnectedStateLayout()
        }
      }
    }
  }
}

@Composable
private fun DeviceTopBar(
  hostConnectionState: HostConnectionState,
  devices: List<HostDevice>,
  selectedDevice: HostDevice?,
  onDeviceClick: (HostDevice) -> Unit,
  onConnectClick: (HostDevice) -> Unit,
  onDisconnectClick: () -> Unit,
  onNavigationItemClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var dropdownExpanded by remember { mutableStateOf(false) }
  Surface(
    modifier = modifier
      .height(56.dp)
      .fillMaxWidth(),
    shadowElevation = 2.dp,
    tonalElevation = 1.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {

      Spacer(Modifier.width(8.dp))

      // Menu button
      IconButton(
        onClick = onNavigationItemClick,
      ) {
        Icon(
          MenuOpen,
          contentDescription = "Menu Open",
        )
      }

      // Connection Indicator
      Box(
        modifier = Modifier
          .size(48.dp),
        contentAlignment = Alignment.Center,
      ) {
        val tint by animateColorAsState(
          when (hostConnectionState) {
            Connected -> Color(0xff118F00)
            Forwarding, Connecting -> Color(0xffD4A017)
            Error -> MaterialTheme.colorScheme.error
            Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
          }
        )

        val pulse = if (hostConnectionState == Forwarding || hostConnectionState == Connecting) {
          val infiniteTransition = rememberInfiniteTransition()
          infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
              animation = tween(800),
              repeatMode = RepeatMode.Reverse,
            ),
          ).value
        } else {
          1f
        }

        Icon(
          imageVector = if (hostConnectionState == Connected) ConnectedIcon else DisconnectedIcon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.alpha(pulse),
        )
      }

      Box {
        OutlinedButton(onClick = { dropdownExpanded = true }) {
          Text(selectedDevice?.displayName ?: "No devices")
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

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            selectedDevice?.let {
              onConnectClick(it)
            }
          },
          enabled = selectedDevice != null && (hostConnectionState == Disconnected || hostConnectionState == Error),
        ) {
          Text("Connect")
        }
        Button(
          onClick = onDisconnectClick,
          enabled = hostConnectionState == Connected || hostConnectionState == Connecting,
        ) {
          Text("Disconnect")
        }
      }

      Spacer(Modifier.weight(1f))

      Switch(
        checked = DebugNodes,
        onCheckedChange = {
          DebugNodes = it
        },
        thumbContent = {
          Icon(
            BugReport,
            contentDescription = null,
          )
        }
      )

      Spacer(Modifier.width(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DrawerContent(
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
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
