package com.livewire.client

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.livewire.LivewireLog
import com.livewire.logDebug
import com.livewire.logError
import com.livewire.transport.DefaultDecoders
import com.livewire.transport.PayloadDecoder
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import com.livewire.ui.composition.LivewireComposition
import com.livewire.ui.actions.LivewireAction
import com.livewire.ui.actions.LocalLivewireActionObserver
import com.livewire.ui.actions.rememberLivewireActionController
import com.livewire.ui.composition.livewireFlow
import com.livewire.ui.data.ClearPlugin
import com.livewire.ui.data.ClientManifest
import com.livewire.ui.data.DarkModeChange
import com.livewire.ui.data.JsonLayoutNodeSerializationStrategy
import com.livewire.ui.data.LayoutNodeSerialization
import com.livewire.ui.data.LayoutNodeSerialization.Json
import com.livewire.ui.data.LayoutNodeSerialization.Protobuf
import com.livewire.ui.data.PluginCrashed
import com.livewire.ui.data.PluginSelected
import com.livewire.ui.data.ProtobufLayoutNodeSerializationStrategy
import com.livewire.ui.data.RequestFullTree
import com.livewire.ui.data.UiDecoders
import com.livewire.ui.data.UiProtocol
import com.livewire.ui.theme.LivewireTheme
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class LivewireClient private constructor(
  val configuration: LivewireClientConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(configure: LivewireClientBuilder.() -> Unit) : this(
    LivewireClientBuilder().apply(configure).build(),
  )

  private val baseContext: CoroutineContext = context
  private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    logError("LivewireClient", "Uncaught error in Livewire scope", throwable)
  }
  private var scope = CoroutineScope(baseContext + SupervisorJob() + exceptionHandler)
  private val darkMode = MutableStateFlow(true)
  private val discoveryBroadcaster = DiscoveryBroadcaster()

  val server = LivewireServer(
    decoders = configuration.decoders + DefaultDecoders + UiDecoders,
    serializationStrategy = when (configuration.layoutNodeSerialization) {
      Json -> JsonLayoutNodeSerializationStrategy()
      Protobuf -> ProtobufLayoutNodeSerializationStrategy()
    },
  )

  fun start() {
    LivewireLog.debugEnabled = configuration.debugLogging

    if (!scope.isActive) {
      scope = CoroutineScope(baseContext + SupervisorJob() + exceptionHandler)
    }

    server.onConnected = {
      val manifest: UiProtocol = ClientManifest(
        theme = configuration.theme,
        layoutNodeSerialization = configuration.layoutNodeSerialization,
        availablePlugins = configuration.plugins
          .map { it.info }
          .toSet(),
      )

      send(manifest)
    }
    server.start()

    discoveryBroadcaster.start(
      scope = scope,
      instanceId = connectionId,
    )

    scope.launchMolecule(RecompositionMode.Immediate, context = LivewireComposition) {
      val connectionState by server.connectionState.collectAsState()
      var activePluginInfo by remember { mutableStateOf<PluginInfo?>(null) }

      val actionController = rememberLivewireActionController()

      val isDarkMode by darkMode.collectAsState()
      val resyncRequests = remember {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
      }

      LaunchedEffect(Unit) {
        server.incomingMessages.collect { message ->
          when (message) {
            is PluginSelected -> activePluginInfo = message.info
            is ClearPlugin -> activePluginInfo = null
            is RequestFullTree -> resyncRequests.tryEmit(Unit)
            is LivewireAction -> actionController.dispatch(message)
            is DarkModeChange -> darkMode.value = message.darkMode
          }
        }
      }

      LaunchedEffect(activePluginInfo, connectionState) {
        if (activePluginInfo != null && connectionState == ConnectionState.Connected) {
          val plugin = configuration.plugins.find { plugin ->
            plugin.info.pluginId == activePluginInfo?.pluginId
          }

          if (plugin != null) {
            try {
              livewireFlow(server.codec.serializationStrategy, resyncRequests) {
                DisposableEffect(Unit) {
                  logDebug("LivewireCompose", "Plugin Entered Composition: ${plugin.info.pluginId}")
                  onDispose {
                    logDebug("LivewireCompose", "Plugin Exited Composition: ${plugin.info.pluginId}")
                  }
                }

                // TODO: REMOVE TESTING
                androidx.compose.material3.Text(
                  text = "UI Composable Text Uh-oh!"
                )

                CompositionLocalProvider(
                  LocalLivewireActionObserver provides actionController,
                ) {
                  LivewireTheme(
                    theme = configuration.theme,
                    darkMode = isDarkMode,
                  ) {
                    plugin.Content()
                  }
                }
              }.collect { output ->
                server.sendLayout(output)
              }
            } catch (e: CancellationException) {
              throw e
            } catch (e: Throwable) {
              // if we're here, the plugin's composition is disposed and can't be recovered. tell the host so it can offer to reload
              logError("LivewireClient", "Plugin '${plugin.info.pluginId}' crashed", e)
              server.send(PluginCrashed(plugin.info.pluginId, e.message))
              activePluginInfo = null
            }
          }
        } else if (activePluginInfo != null) {
          // If we disconnect, be sure to clear the active plugin state
          activePluginInfo = null
        }
      }
    }
  }

  fun stop() {
    scope.cancel()
    server.stop()
  }
}

@LivewireClientDsl
class LivewireClientBuilder {
  private var theme: LivewireTheme? = null
  private val plugins = mutableSetOf<Plugin>()
  private val decoders = mutableSetOf<PayloadDecoder<*>>()
  private var layoutNodeSerialization = Protobuf
  private var debugLogging = false

  fun install(plugin: Plugin) {
    plugins.add(plugin)
  }

  fun debugLogging(enabled: Boolean = true) {
    debugLogging = enabled
  }

  fun theme(theme: LivewireTheme) {
    this.theme = theme
  }

  fun layoutNodeSerialization(
    strategy: LayoutNodeSerialization,
  ) {
    layoutNodeSerialization = strategy
  }

  fun build(): LivewireClientConfiguration {
    return LivewireClientConfiguration(
      theme = theme ?: LivewireTheme(),
      plugins = plugins,
      decoders = decoders,
      layoutNodeSerialization = layoutNodeSerialization,
      debugLogging = debugLogging,
    )
  }
}

class LivewireClientConfiguration(
  val theme: LivewireTheme,
  val plugins: Set<Plugin>,
  val decoders: Set<PayloadDecoder<*>>,
  val layoutNodeSerialization: LayoutNodeSerialization,
  val debugLogging: Boolean,
)

@DslMarker
annotation class LivewireClientDsl
