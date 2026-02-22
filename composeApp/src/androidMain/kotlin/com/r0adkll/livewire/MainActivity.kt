package com.r0adkll.livewire

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.client.LivewireServer
import com.r0adkll.livewire.plugin.chat.client.ChatDataConnector
import com.r0adkll.livewire.protocol.SimpleMessage

class MainActivity : ComponentActivity() {

  private val livewireClient: LivewireClient = ServiceLocator.livewireClient

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    livewireClient.start()

    setContent {
      MaterialTheme {
        val connectionState by livewireClient.server.connectionState.collectAsState()
        val messages = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
          livewireClient.server.incomingMessages.collect { envelope ->
            messages.add("$envelope")
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

          val pagerState = rememberPagerState(2) { 2 }
          HorizontalPager(
            state = pagerState,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) { page ->
            when (page) {
              0 -> {
                MessagePage(
                  messages = messages,
                )
              }
              1 -> {
                val dataConnector = remember {
                  livewireClient.connector<ChatDataConnector>()!!
                }

                ChatPage(dataConnector)
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun MessagePage(
    messages: List<String>,
    modifier: Modifier = Modifier,
  ) {
    LazyColumn(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(messages) { msg ->
        Text(msg, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }

  @Composable
  private fun ChatPage(
    dataConnector: ChatDataConnector,
    modifier: Modifier = Modifier,
  ) {
    LazyColumn(
      modifier = modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items(dataConnector.messages) { msg ->
        Text(msg, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    livewireClient.stop()
  }
}
