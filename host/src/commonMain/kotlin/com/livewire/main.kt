package com.livewire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.livewire.runtime.HostConnectionState
import com.livewire.runtime.HostConnectionState.Connected
import com.livewire.runtime.LivewireHost
import com.livewire.runtime.discoverymanager.CompositeDiscoveryManager
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.settings.LivewireSettings
import com.livewire.settings.observe
import com.livewire.settings.rememberLivewireSettings
import com.livewire.theme.LivewireThemeContent
import com.livewire.ui.PluginDrawerItem
import com.livewire.ui.PluginInfo
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.composables.ConnectionStatusChip
import com.livewire.ui.composables.DisconnectedStateLayout
import com.livewire.ui.data.ClientManifest
import com.livewire.ui.data.DarkModeChange
import com.livewire.ui.data.PluginSelected
import com.livewire.host.ui.DebugNodes
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.ui.snackbar.LocalSnackDispatcher
import com.livewire.ui.snackbar.rememberSnackbarDispatcher
import com.livewire.ui.icons.BugReport
import com.livewire.ui.icons.DarkMode
import com.livewire.ui.icons.LightMode
import com.livewire.ui.icons.MenuOpen
import com.livewire.ui.layout.HostDrawerSheet
import com.livewire.ui.layout.HostScaffold
import com.livewire.ui.theme.LivewireTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3ExpressiveApi::class)
fun main() = application {
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
      onDisconnect = {
        reconnectTargetId = null
        scope.launch { host.connection.disconnect() }
      },
      onConnect = { app ->
        reconnectTargetId = app.id
        scope.launch { host.connection.connect(app) }
      },
    )
  }
}

@Composable
private fun AppUi(
  scope: CoroutineScope,
  settings: LivewireSettings,
  devicesReady: Boolean,
  host: LivewireHost,
  state: HostConnectionState,
  apps: List<HostApp>,
  clientManifest: ClientManifest?,
  selectedPlugin: PluginInfo?,
  onPluginClick: (PluginInfo) -> Unit,
  onDisconnect: () -> Unit,
  onConnect: (HostApp) -> Unit,
  modifier: Modifier = Modifier,
) {
  val systemDarkMode = isSystemInDarkTheme()
  var isDarkMode by remember { mutableStateOf(systemDarkMode) }
  LaunchedEffect(host.connection) {
    host.connection.incomingMessages
      .filterIsInstance<DarkModeChange>()
      .collect {
        isDarkMode = it.darkMode
      }
  }

  LivewireThemeContent(
    theme = clientManifest?.theme ?: LivewireTheme(),
    // TODO: Should have a host setting that defaults back to host choice
    darkMode = isDarkMode,
    host = host,
  ) {
    val menuExpanded by remember {
      settings::menuExpanded.observe()
    }.collectAsState()

    var selectedApp: HostApp? by remember { mutableStateOf(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarDispatcher = rememberSnackbarDispatcher(snackbarHostState)
    HostScaffold(
      topBar = {
        AppTopBar(
          darkMode = isDarkMode,
          onDarkModeChanged = { isDarkMode = it },
          hostConnectionState = state,
          selectedApp = selectedApp,
          onDisconnectClick = { scope.launch { host.connection.disconnect() } },
          onNavigationItemClick = {
            settings.menuExpanded = !menuExpanded
          },
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
              onPluginClick = onPluginClick,
            )
          }
        }
      },
      snackbarHost = {
        SnackbarHost(
          hostState = snackbarHostState,
          snackbar = {
            Snackbar(
              snackbarData = it,
              shape = MaterialTheme.shapes.medium,
            )
          },
          modifier = Modifier
            .align(Alignment.BottomEnd),
        )
      },
      modifier = modifier,
    ) {
      val layoutNode by host.connection.incomingLayoutNodes.collectAsState()
      CompositionLocalProvider(
        LocalLivewireActionDispatcher provides host,
        LocalSnackDispatcher provides snackbarDispatcher,
      ) {
        key(selectedPlugin?.pluginId) {
          LayoutNodeContent(
            node = layoutNode,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }

      if (state != Connected) {
        DisconnectedStateLayout(
          apps = apps,
          devicesReady = devicesReady,
          state = state,
          onConnectClick = { app ->
            selectedApp = app
            onConnect(app)
          },
          onDisconnectClick = {
            selectedApp = null
            onDisconnect()
          },
        )
      }
    }
  }
}

@Composable
private fun AppTopBar(
  darkMode: Boolean,
  onDarkModeChanged: (Boolean) -> Unit,
  hostConnectionState: HostConnectionState,
  selectedApp: HostApp?,
  onDisconnectClick: () -> Unit,
  onNavigationItemClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .height(48.dp)
      .fillMaxWidth(),
    shadowElevation = 2.dp,
    tonalElevation = 1.dp,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 8.dp),
    ) {
      IconButton(
        onClick = onNavigationItemClick,
        enabled = hostConnectionState == Connected,
      ) {
        Icon(
          MenuOpen,
          contentDescription = "Toggle menu",
          modifier = Modifier.size(20.dp),
        )
      }

      ConnectionStatusChip(
        state = hostConnectionState,
        selectedApp = selectedApp,
        onDisconnectClick = onDisconnectClick,
      )

      Spacer(Modifier.weight(1f))

      // TODO: Hide behind a 'Debug' build flag
      Switch(
        checked = DebugNodes,
        onCheckedChange = { DebugNodes = it },
        thumbContent = {
          Icon(
            BugReport,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
          )
        },
      )

      Spacer(Modifier.width(8.dp))

      Switch(
        checked = darkMode,
        onCheckedChange = onDarkModeChanged,
        thumbContent = {
          Icon(
            if (darkMode) DarkMode else LightMode,
            contentDescription = null,
            modifier = Modifier.size(SwitchDefaults.IconSize),
            tint = Color.White
          )
        },
      )

      Spacer(Modifier.width(8.dp))
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
