package com.livewire.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.livewire.host.ui.LayoutNodeContent
import com.livewire.runtime.HostConnectionState
import com.livewire.runtime.HostConnectionState.Connected
import com.livewire.runtime.LivewireHost
import com.livewire.runtime.discoverymanager.HostApp
import com.livewire.settings.LivewireSettings
import com.livewire.settings.observe
import com.livewire.theme.LivewireThemeContent
import com.livewire.ui.actions.LocalLivewireActionDispatcher
import com.livewire.ui.composables.AppTopBar
import com.livewire.ui.composables.DisconnectedStateLayout
import com.livewire.ui.data.ClientManifest
import com.livewire.ui.data.DarkModeChange
import com.livewire.ui.layout.HostDrawerSheet
import com.livewire.ui.layout.HostScaffold
import com.livewire.ui.snackbar.LocalSnackDispatcher
import com.livewire.ui.snackbar.rememberSnackbarDispatcher
import com.livewire.ui.theme.LivewireTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@Composable
internal fun AppUi(
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
