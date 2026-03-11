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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.r0adkll.livewire.runtime.DevicePreferences
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.HostConnectionState.Error
import com.r0adkll.livewire.runtime.LivewireHost
import com.r0adkll.livewire.runtime.devicemanager.AdbDevice
import com.r0adkll.livewire.runtime.devicemanager.CompositeDeviceManager
import com.r0adkll.livewire.runtime.devicemanager.HostDevice
import com.r0adkll.livewire.runtime.devicemanager.IosDevice
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
import com.r0adkll.livewire.ui.icons.AndroidIcon
import com.r0adkll.livewire.ui.icons.AppleIcon
import com.r0adkll.livewire.ui.icons.BugReport
import com.r0adkll.livewire.ui.icons.ChevronDown
import com.r0adkll.livewire.ui.icons.ConnectedIcon
import com.r0adkll.livewire.ui.icons.DisconnectedIcon
import com.r0adkll.livewire.ui.icons.MenuOpen
import com.r0adkll.livewire.ui.layout.HostDrawerSheet
import com.r0adkll.livewire.ui.layout.HostScaffold
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
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
  var devicesReady by remember { mutableStateOf(false) }
  var initialSelectionDone by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    CompositeDeviceManager.deviceList().collect { deviceList ->
      devices = deviceList

      if (selectedDevice == null || deviceList.none { selectedDevice?.id == it.id }) {
        if (initialSelectionDone) {
          selectedDevice = deviceList.firstOrNull()
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    CompositeDeviceManager.isReady().first { it }
    devicesReady = true

    val lastId = DevicePreferences.lastConnectedDeviceId
    if (selectedDevice == null) {
      selectedDevice = devices.firstOrNull { it.id == lastId } ?: devices.firstOrNull()
    }
    initialSelectionDone = true
  }

  var clientManifest by remember { mutableStateOf<ClientManifest?>(null) }
  var selectedPlugin by remember { mutableStateOf<PluginInfo?>(null) }

  LaunchedEffect(state) {
    if (state == Connected) {
      selectedDevice?.let { DevicePreferences.lastConnectedDeviceId = it.id }
    }
    if (state != Connected) {
      selectedPlugin = null
      clientManifest = null
    }
  }

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
            devicesReady = devicesReady,
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
  devicesReady: Boolean,
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
        enabled = hostConnectionState == Connected,
      ) {
        Icon(
          MenuOpen,
          contentDescription = "Menu Open",
        )
      }

      Box(
        modifier = Modifier
          .size(48.dp),
        contentAlignment = Alignment.Center,
      ) {
        val tint by animateColorAsState(
          when (hostConnectionState) {
            Connected -> Color(0xff118F00)
            Forwarding, Listening -> Color(0xffD4A017)
            Error -> MaterialTheme.colorScheme.error
            Disconnected -> MaterialTheme.colorScheme.onSurfaceVariant
          }
        )

        val pulse = if (hostConnectionState == Forwarding || hostConnectionState == Listening) {
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

      DeviceSelector(
        devices = devices,
        devicesReady = devicesReady,
        selectedDevice = selectedDevice,
        dropdownExpanded = dropdownExpanded,
        onExpandClick = { dropdownExpanded = true },
        onDismiss = { dropdownExpanded = false },
        onDeviceClick = { device ->
          onDeviceClick(device)
          dropdownExpanded = false
        },
      )

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
          enabled = hostConnectionState == Connected || hostConnectionState == Forwarding || hostConnectionState == Listening,
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

@Composable
private fun DeviceSelector(
  devices: List<HostDevice>,
  devicesReady: Boolean,
  selectedDevice: HostDevice?,
  dropdownExpanded: Boolean,
  onExpandClick: () -> Unit,
  onDismiss: () -> Unit,
  onDeviceClick: (HostDevice) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    Surface(
      onClick = onExpandClick,
      enabled = devicesReady && devices.isNotEmpty(),
      shape = MaterialTheme.shapes.small,
      tonalElevation = 4.dp,
    ) {
      Row(
        modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        if (!devicesReady) {
          LinearProgressIndicator(
            modifier = Modifier.width(100.dp),
          )
          Text(
            text = "Scanning…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else if (selectedDevice != null) {
          Icon(
            imageVector = selectedDevice.platformIcon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = selectedDevice.displayName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        } else {
          Text(
            text = "No devices",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Icon(
          imageVector = ChevronDown,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    DropdownMenu(
      expanded = dropdownExpanded,
      onDismissRequest = onDismiss,
    ) {
      val androidDevices = devices.filterIsInstance<AdbDevice>()
      val iosDevices = devices.filterIsInstance<IosDevice>()

      if (androidDevices.isNotEmpty()) {
        DeviceSectionHeader(title = "Android")
        androidDevices.forEach { device ->
          DeviceDropdownItem(
            device = device,
            selected = device.id == selectedDevice?.id,
            primaryText = device.model.ifEmpty { device.serial },
            secondaryText = if (device.model.isNotEmpty() && device.model != device.serial) device.serial else null,
            onClick = { onDeviceClick(device) },
          )
        }
      }

      if (androidDevices.isNotEmpty() && iosDevices.isNotEmpty()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
      }

      if (iosDevices.isNotEmpty()) {
        DeviceSectionHeader("iOS")
        iosDevices.forEach { device ->
          DeviceDropdownItem(
            device = device,
            selected = device.id == selectedDevice?.id,
            primaryText = device.name,
            secondaryText = "iOS ${device.osVersion}${if (device.deviceType == Simulator) " Simulator" else ""}",
            onClick = { onDeviceClick(device) },
          )
        }
      }

      if (devices.isEmpty()) {
        DropdownMenuItem(
          text = {
            Text(
              text = "No devices found",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          onClick = {},
          enabled = false,
        )
      }
    }
  }
}

@Composable
private fun DeviceSectionHeader(
  title: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = title,
    modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    style = MaterialTheme.typography.labelSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun DeviceDropdownItem(
  device: HostDevice,
  selected: Boolean,
  primaryText: String,
  secondaryText: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  DropdownMenuItem(
    modifier = modifier,
    text = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = device.platformIcon,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
          Text(
            text = primaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
          )
          if (secondaryText != null) {
            Text(
              text = secondaryText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    },
    onClick = onClick,
  )
}

private val HostDevice.platformIcon: ImageVector
  get() = when (this) {
    is AdbDevice -> AndroidIcon
    is IosDevice -> AppleIcon
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
