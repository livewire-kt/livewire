package com.r0adkll.livewire.client.connector

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.r0adkll.livewire.DataConnector
import com.r0adkll.livewire.client.LivewireClient

@Composable
internal fun DataConnectorContent(
  client: LivewireClient,
  connector: DataConnector<*, *>,
) {
  DisposableEffect(Unit) {
    Log.d("DataConnector", "DataConnector Connected: $connector")

    connector.hostSink = {

    }

    onDispose {
      Log.d("DataConnector", "DataConnector Disconnected: $connector")
      connector.hostSink = {}
    }
  }

  LaunchedEffect(Unit) {
    client.ingestMessages(connector)
  }

  connector.Connect()
}
