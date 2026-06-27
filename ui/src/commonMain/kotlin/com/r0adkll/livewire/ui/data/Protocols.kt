package com.r0adkll.livewire.ui.data

import androidx.compose.runtime.Immutable
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.theme.LivewireTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
sealed interface UiProtocol {
  companion object : PayloadDecoder<UiProtocol> {
    override fun Json.decodePayload(element: JsonElement): UiProtocol = decodeFromJsonElement(serializer(), element)
  }
}

/**
 * Sent by the client application to the host application so that it can enumerate the list of available plugins
 * to the user to browse and interact with
 */
@Serializable
@Immutable
data class ClientManifest(
  val theme: LivewireTheme,
  val layoutNodeSerialization: LayoutNodeSerialization,
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
) : UiProtocol {

  override fun toString(): String {
    return "PluginSelected[${info.title}]"
  }
}

/**
 * Sent by the Host to clear any plugins actively rendering their content for the server
 */
@Serializable
data object ClearPlugin : UiProtocol

/**
 * Sent by the host when it detects that its diff'd layout tree has desynced from the client. The client will respond by restarting
 * the active plugin's composition and emitting a fresh full tree.
 */
@Serializable
data object RequestFullTree : UiProtocol


val UiDecoders: List<PayloadDecoder<*>> get() = listOf(
  UiProtocol,
  LivewireAction,
)
