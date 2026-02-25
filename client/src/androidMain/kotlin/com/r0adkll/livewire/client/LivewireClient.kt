package com.r0adkll.livewire.client

import android.util.Log
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
import com.r0adkll.livewire.ui.data.PluginSelected
import com.r0adkll.livewire.ui.data.UiDecoders
import com.r0adkll.livewire.ui.data.UiProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LivewireClient private constructor(
  val configuration: LivewireClientConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(configure: LivewireClientBuilder.() -> Unit) : this(
    LivewireClientBuilder().apply(configure).build()
  )

  private val scope = CoroutineScope(context + SupervisorJob())

  val server = LivewireServer(
    decoders = configuration.decoders + DefaultDecoders + UiDecoders,
  )

  @OptIn(ExperimentalCoroutinesApi::class)
  fun start() {
    server.start()

    scope.launch {
      server.connectionState.collect { connectionState ->
        when (connectionState) {
          ConnectionState.STARTED,
          ConnectionState.STOPPED,
          ConnectionState.ERROR -> Unit

          ConnectionState.CONNECTED -> {
            // Send ClientManifest to the new host connection
            val manifest: UiProtocol = ClientManifest(
              configuration.plugins
                .map { it.info }
                .toSet()
            )

            server.send(manifest)
          }
        }
      }
    }

    scope.launchMolecule(RecompositionMode.Immediate) {
      val connectionState by server.connectionState.collectAsState()
      var activePluginInfo by remember { mutableStateOf<PluginInfo?>(null) }

      val actionController = rememberLivewireActionController()

      LaunchedEffect(Unit) {
        server.incomingMessages.collect { message ->
          when (message) {
            is PluginSelected -> {
              activePluginInfo = message.info
            }

            is ClearPlugin -> {
              activePluginInfo = null
            }

            is LivewireAction -> {
              actionController.dispatch(message)
            }
          }
        }
      }

      LaunchedEffect(activePluginInfo, connectionState) {
        if (activePluginInfo != null && connectionState == ConnectionState.CONNECTED) {
          val plugin = configuration.plugins.find { plugin ->
            plugin.info.pluginId == activePluginInfo?.pluginId
          }

          if (plugin != null) {
            livewireFlow {
              DisposableEffect(Unit) {
                Log.d("LivewireCompose", "Plugin Entered Composition: ${plugin.info.pluginId}")
                onDispose {
                  Log.d("LivewireCompose", "Plugin Exited Composition: ${plugin.info.pluginId}")
                }
              }

              CompositionLocalProvider(
                LocalLivewireActionObserver provides actionController,
              ) {
                plugin.Content()
              }
            }.collect { layoutNode ->
              server.sendLayoutNode(layoutNode)
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
  val plugins = mutableSetOf<Plugin>()
  val decoders = mutableSetOf<PayloadDecoder<*>>()

  fun install(plugin: Plugin) {
    plugins.add(plugin)
  }

  fun build(): LivewireClientConfiguration {
    return LivewireClientConfiguration(
      plugins = plugins,
      decoders = decoders,
    )
  }
}

class LivewireClientConfiguration(
  val plugins: Set<Plugin>,
  val decoders: Set<PayloadDecoder<*>>,
)

@DslMarker
annotation class LivewireClientDsl
