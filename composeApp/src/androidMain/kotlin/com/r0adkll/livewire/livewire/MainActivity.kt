package com.r0adkll.livewire.livewire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.runtime.LivewireServer

class MainActivity : ComponentActivity() {

    private val livewireServer = LivewireServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        livewireServer.start()

        setContent {
            MaterialTheme {
                val connectionState by livewireServer.connectionState.collectAsState()
                val messages = remember { mutableStateListOf<String>() }

                LaunchedEffect(Unit) {
                    livewireServer.incomingMessages.collect { msg ->
                        messages.add(msg)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Livewire Server: $connectionState",
                        style = MaterialTheme.typography.titleMedium,
                    )

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

    override fun onDestroy() {
        super.onDestroy()
        livewireServer.stop()
    }
}
