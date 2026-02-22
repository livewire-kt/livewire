package com.r0adkll.livewire.client.connector

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.r0adkll.livewire.DataConnector
import com.r0adkll.livewire.client.LivewireClient
import kotlinx.coroutines.launch

@Composable
internal fun DataConnectorContent(
  client: LivewireClient,
  connector: DataConnector<*, *>,
) {
  val scope = rememberCoroutineScope()

  DisposableEffect(Unit) {
    Log.d("DataConnector", "DataConnector Connected: $connector")
    connector.isConnected = true
    connector.hostSink = {
      scope.launch {
        client.send(connector, it)
      }
    }

    onDispose {
      Log.d("DataConnector", "DataConnector Disconnected: $connector")
      connector.isConnected = false
      connector.hostSink = {}
    }
  }

  LaunchedEffect(Unit) {
    client.ingestMessages(connector)
  }

  connector.Connect()
}
