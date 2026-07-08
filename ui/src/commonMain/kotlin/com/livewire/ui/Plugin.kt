package com.livewire.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.graphics.toVectorIcon
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

@Immutable
@Serializable
data class PluginInfo(
  val pluginId: String,
  val title: String,
  /** Plugin icon, rendered as a vector on the host. */
  val icon: VectorIcon? = null,
) {
  constructor(
    pluginId: String,
    title: String,
    icon: ImageVector,
  ) : this(pluginId, title, icon.toVectorIcon())
}
