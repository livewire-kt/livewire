package com.livewire

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.livewire.runtime.HostConnectionState.Connected
import com.livewire.runtime.LivewireHost
import com.livewire.runtime.discoverymanager.CompositeDiscoveryManager
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.settings.observe
import com.livewire.settings.rememberLivewireSettings
import com.livewire.ui.AppUi
import com.livewire.ui.NetworkMeterWindow
import com.livewire.ui.PluginInfo
import com.livewire.ui.data.ClientManifest
import com.livewire.ui.data.DarkModeChange
import com.livewire.ui.data.PluginSelected
import com.livewire.ui.theme.LivewireTheme
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
fun main() = application {
  LivewireLog.debugEnabled = true

  val settings = rememberLivewireSettings()

  val windowState = rememberWindowState(
    size = DpSize(settings.windowWidth.dp, settings.windowHeight.dp),
    position = if (settings.hasWindowPosition) {
      WindowPosition.Absolute(settings.windowX.dp, settings.windowY.dp)
    } else {
      WindowPosition.PlatformDefault
    },
  )

  val host = remember { LivewireHost() }
  LaunchedEffect(Unit) {
    Runtime.getRuntime().addShutdownHook(
      Thread {
        runBlocking {
          host.connection.close()
        }

        CompositeDiscoveryManager.shutdown()

        settings.windowWidth = windowState.size.width.value.toInt()
        settings.windowHeight = windowState.size.height.value.toInt()
        val position = windowState.position
        if (position is WindowPosition.Absolute) {
          settings.windowX = position.x.value.toInt()
          settings.windowY = position.y.value.toInt()
        }
      },
    )
  }

  val scope = rememberCoroutineScope()
  val state by host.connection.connectionState.collectAsState()

  var apps by remember { mutableStateOf<List<HostApp>>(emptyList()) }
  var devicesReady by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    CompositeDiscoveryManager.appList().collect { appList ->
      apps = appList
    }
  }

  LaunchedEffect(Unit) {
    CompositeDiscoveryManager.isReady().first { it }
    devicesReady = true
  }

  var clientManifest by remember { mutableStateOf<ClientManifest?>(null) }
  var selectedPlugin by remember { mutableStateOf<PluginInfo?>(null) }
  var showNetworkMeter by remember { mutableStateOf(false) }

  var reconnectTargetId by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(state) {
    if (state != Connected) {
      selectedPlugin = null
      clientManifest = null
    }
  }

  LaunchedEffect(reconnectTargetId, apps, state) {
    val targetId = reconnectTargetId ?: return@LaunchedEffect
    if (state != Listening) return@LaunchedEffect
    val matchingApp = apps.firstOrNull { it.id == targetId }
    if (matchingApp != null) {
      logDebug("auto-reconnect", "reconnecting to ${matchingApp.id} (instanceId=${matchingApp.instanceId})")
      host.connection.connect(matchingApp)
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

        // Tell the newly connected client what our current dark mode setting is
        host.connection.send(DarkModeChange(settings.darkMode))
      }
  }

  Window(
    onCloseRequest = {
      settings.windowWidth = windowState.size.width.value.toInt()
      settings.windowHeight = windowState.size.height.value.toInt()
      val position = windowState.position
      if (position is WindowPosition.Absolute) {
        settings.windowX = position.x.value.toInt()
        settings.windowY = position.y.value.toInt()
      }
      exitApplication()
    },
    title = "Livewire Host",
    state = windowState,
  ) {
    AppUi(
      scope = scope,
      settings = settings,
      devicesReady = devicesReady,
      host = host,
      state = state,
      apps = apps,
      clientManifest = clientManifest,
      selectedPlugin = selectedPlugin,
      onPluginClick = { plugin ->
        selectedPlugin = plugin
        scope.launch {
          host.connection.send(PluginSelected(plugin))
        }
      },
      onReloadPlugin = { pluginId ->
        clientManifest?.availablePlugins?.firstOrNull { it.pluginId == pluginId }?.let { plugin ->
          selectedPlugin = plugin
          scope.launch {
            host.connection.send(PluginSelected(plugin))
          }
        }
      },
      onDisconnect = {
        reconnectTargetId = null
        scope.launch { host.connection.disconnect() }
      },
      onConnect = { app ->
        reconnectTargetId = app.id
        scope.launch { host.connection.connect(app) }
      },
      onNetworkMeterClick = { showNetworkMeter = true },
    )
  }

  if (showNetworkMeter) {
    val isDarkMode by remember { settings::darkMode.observe() }.collectAsState()
    NetworkMeterWindow(
      host = host,
      theme = clientManifest?.theme ?: LivewireTheme(),
      darkMode = isDarkMode,
      onCloseRequest = { showNetworkMeter = false },
    )
  }
}

