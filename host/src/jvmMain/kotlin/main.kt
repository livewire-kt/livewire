package com.r0adkll.livewire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.r0adkll.livewire.runtime.HostConnectionState
import com.r0adkll.livewire.runtime.LivewireHostConnection
import kotlinx.coroutines.launch

fun main() = application {
    val connection = remember { LivewireHostConnection() }
    val scope = rememberCoroutineScope()
    val state by connection.connectionState.collectAsState()
    val messages = remember { mutableStateListOf<String>() }

    // Collect incoming messages
    scope.launch {
        connection.incomingMessages.collect { msg ->
            messages.add(msg)
        }
    }

    Window(
        onCloseRequest = {
            connection.close()
            exitApplication()
        },
        title = "Livewire Host",
    ) {
        MaterialTheme {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Connection: $state", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { connection.connect() },
                        enabled = state == HostConnectionState.DISCONNECTED || state == HostConnectionState.ERROR,
                    ) {
                        Text("Connect")
                    }
                    Button(
                        onClick = { scope.launch { connection.send("ping") } },
                        enabled = state == HostConnectionState.CONNECTED,
                    ) {
                        Text("Send Ping")
                    }
                    Button(
                        onClick = { connection.disconnect() },
                        enabled = state == HostConnectionState.CONNECTED,
                    ) {
                        Text("Disconnect")
                    }
                }

                Text("Messages:", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(messages) { msg ->
                        Text(msg, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
