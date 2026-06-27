package com.r0adkll.livewire.client

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
import com.r0adkll.livewire.logDebug
import com.r0adkll.livewire.transport.DefaultDecoders
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.actions.LocalLivewireActionObserver
import com.r0adkll.livewire.ui.actions.rememberLivewireActionController
import com.r0adkll.livewire.ui.composition.livewireFlow
import com.r0adkll.livewire.ui.data.ClearPlugin
import com.r0adkll.livewire.ui.data.ClientManifest
import com.r0adkll.livewire.ui.data.DarkModeChange
import com.r0adkll.livewire.ui.data.JsonLayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.data.LayoutNodeSerialization
import com.r0adkll.livewire.ui.data.LayoutNodeSerialization.Json
import com.r0adkll.livewire.ui.data.LayoutNodeSerialization.Protobuf
import com.r0adkll.livewire.ui.data.PluginSelected
import com.r0adkll.livewire.ui.data.RequestFullTree
import com.r0adkll.livewire.ui.data.ProtobufLayoutNodeSerializationStrategy
import com.r0adkll.livewire.ui.data.UiDecoders
import com.r0adkll.livewire.ui.data.UiProtocol
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
class LivewireClient private constructor(
  val configuration: LivewireClientConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(configure: LivewireClientBuilder.() -> Unit) : this(
    LivewireClientBuilder().apply(configure).build(),
  )

  private val scope = CoroutineScope(context + SupervisorJob())

  private val discoveryBroadcaster = DiscoveryBroadcaster()

  val server = LivewireServer(
    decoders = configuration.decoders + DefaultDecoders + UiDecoders,
    serializationStrategy = when (configuration.layoutNodeSerialization) {
      Json -> JsonLayoutNodeSerializationStrategy()
      Protobuf -> ProtobufLayoutNodeSerializationStrategy()
    },
  )

  fun start() {
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

    scope.launchMolecule(RecompositionMode.Immediate) {
      val connectionState by server.connectionState.collectAsState()
      var activePluginInfo by remember { mutableStateOf<PluginInfo?>(null) }

      val actionController = rememberLivewireActionController()

      var isDarkMode by remember { mutableStateOf(false) }
      var resyncToken by remember { mutableStateOf(0) }

      LaunchedEffect(Unit) {
        server.incomingMessages.collect { message ->
          when (message) {
            is DarkModeChange -> {
              isDarkMode = message.darkMode
            }

            is PluginSelected -> {
              activePluginInfo = message.info
            }

            is ClearPlugin -> {
              activePluginInfo = null
            }

            is RequestFullTree -> {
              resyncToken++
            }

            is LivewireAction -> {
              actionController.dispatch(message)
            }
          }
        }
      }

      LaunchedEffect(activePluginInfo, connectionState, resyncToken) {
        if (activePluginInfo != null && connectionState == ConnectionState.Connected) {
          val plugin = configuration.plugins.find { plugin ->
            plugin.info.pluginId == activePluginInfo?.pluginId
          }

          if (plugin != null) {
            livewireFlow(server.codec.serializationStrategy) {
              DisposableEffect(Unit) {
                logDebug("LivewireCompose", "Plugin Entered Composition: ${plugin.info.pluginId}")
                onDispose {
                  logDebug("LivewireCompose", "Plugin Exited Composition: ${plugin.info.pluginId}")
                }
              }

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

  fun install(plugin: Plugin) {
    plugins.add(plugin)
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
    )
  }
}

class LivewireClientConfiguration(
  val theme: LivewireTheme,
  val plugins: Set<Plugin>,
  val decoders: Set<PayloadDecoder<*>>,
  val layoutNodeSerialization: LayoutNodeSerialization,
)

@DslMarker
annotation class LivewireClientDsl
