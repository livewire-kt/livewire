package com.r0adkll.livewire.ui

import androidx.compose.runtime.Composable
import com.r0adkll.livewire.ui.composition.LivewireComposable
import kotlinx.serialization.Serializable

/**
 * A simple interface for plugins to define how they are render data connector content
 * in the host application.
 */
interface Plugin {

  /**
   * How this HostUi appears in the host application to provide the user a means
   * to click / and render the below UI
   */
  val info: PluginInfo

  /**
   * Custom composition to build UI to be remotely rendered. This will essentially be a basic catalog of
   * predefined components that can be packaged into json/binary format and sent across the wire
   */
  @LivewireComposable
  @Composable
  fun Content()
}

@Serializable
data class PluginInfo(
  val pluginId: String,
  val iconData: String,
  val title: String,
)
