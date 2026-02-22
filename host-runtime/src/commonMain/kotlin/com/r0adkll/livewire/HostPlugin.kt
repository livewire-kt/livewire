package com.r0adkll.livewire

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent
import com.r0adkll.livewire.transport.events.DatabaseClientEvent
import com.r0adkll.livewire.transport.events.DatabaseHostEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class HostPlugin<HostT : HostEvent, ClientT : ClientEvent>() {

  private val _hostEvents = MutableSharedFlow<HostT>(0, extraBufferCapacity = 64)
  protected val hostEvents: SharedFlow<HostT> = _hostEvents.asSharedFlow()
  var clientSink: (ClientT) -> Unit = {}

  suspend fun emitEvent(event: HostT) {
    _hostEvents.emit(event)
  }

  @Composable
  abstract fun createPresentation(): Presentation

  @Composable
  abstract fun Content(
    modifier: Modifier,
  )

  data class Presentation(
    val icon: ImageVector,
    val title: String,
  )
}

// TODO: This is just a sample to explore API / DSLs

class DatabaseHostPlugin : HostPlugin<DatabaseHostEvent, DatabaseClientEvent>() {

  @Composable
  override fun createPresentation(): Presentation {
    TODO("Not yet implemented")
  }

  @Composable
  override fun Content(modifier: Modifier) {
    var databases by remember { mutableStateOf(emptyList<String>()) }
    var selectedDatabase by remember { mutableStateOf<String?>(null) }

    val tables = remember { mutableStateMapOf<String, List<String>>() }
    val tableSchemas = remember { mutableStateMapOf<String, List<List<String>>>() }

    // Collect the host events from the socket connection that are meant
    // for this plugin. Using them to modify its state held in its composition.
    LaunchedEffect(Unit) {
      hostEvents.collect { hostEvent ->
        when (hostEvent) {
          is DatabaseHostEvent.DatabaseList -> databases = hostEvent.names
          is DatabaseHostEvent.TableList -> TODO()
          is DatabaseHostEvent.TableSchema -> TODO()
          is DatabaseHostEvent.QueryResults -> TODO()
        }
      }
    }

    // TODO: UI Code to render the data collected from host events. Developers will
    //  be able to use a collection of pre-built widgets to easily display data
    //  in the host application

    Column {
      Button(
        onClick = {
          // Send a ClientEvent to the client, asking it to send
          // more/new data back over
          clientSink(DatabaseClientEvent.GetDatabases)
        }
      ) {
        Text("Refresh Databases")
      }

    }
  }
}
