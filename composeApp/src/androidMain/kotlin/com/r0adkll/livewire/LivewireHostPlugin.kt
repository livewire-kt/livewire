package com.r0adkll.livewire

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.invalidateGroupsWithKey
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.r0adkll.livewire.client.ConnectionState
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.ui.composition.livewireFlow
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.widget.Text
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class LivewireHostPlugin(
  private val client: LivewireClient,
) {

  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun renderHostPlugin() {
    client.server.connectionState
      .flatMapLatest {
        if (it != ConnectionState.CONNECTED) return@flatMapLatest emptyFlow()
        livewireFlow {
          val connectionState by client.server.connectionState.collectAsState()
          Column {
            Text("Connected: ${connectionState.name}")

            Text("Text 1")
            Text("Text 2")
            Text("Text 3")
            Text("Text 4")

            val messages = remember { mutableStateListOf<String>() }
            messages.forEach { message ->
              Text(message)
            }

            LaunchedEffect(Unit) {
              var count = 0
              while (isActive) {
                delay(3.seconds)
                count++
                messages += "Msg $count"
              }
            }
          }
        }
      }.collect { layoutNode ->
        client.server.sendLayoutNode(layoutNode)
      }
  }
}
