package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface UiProtocol {
  companion object : PayloadDecoder<UiProtocol> {
    override suspend fun Json.decodePayload(rawPayload: String): UiProtocol {
      return decodeFromString(serializer(), rawPayload)
    }

  }
}

/**
 * Sent by the client application to the host application so that it can enumerate the list of available plugins
 * to the user to browse and interact with
 */
@Serializable
data class ClientManifest(
  val theme: LivewireTheme,
  val availablePlugins: Set<PluginInfo>,
) : UiProtocol

/**
 * Sent by host to set the dark mode on the client side themeing
 * tokens.
 */
@Serializable
data class DarkModeChange(
  val darkMode: Boolean,
) : UiProtocol {
}

/**
 * Sent by Host to signal client to start producing a [com.r0adkll.livewire.ui.Plugin] content
 * to display.
 */
@Serializable
data class PluginSelected(
  val info: PluginInfo,
) : UiProtocol

/**
 * Sent by the Host to clear any plugins actively rendering their content for the server
 */
@Serializable
data object ClearPlugin : UiProtocol


val UiDecoders: List<PayloadDecoder<*>> get() = listOf(
  UiProtocol,
  LivewireAction,
)
