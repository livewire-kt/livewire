package com.r0adkll.livewire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.client.LivewireServer
import com.r0adkll.livewire.plugin.chat.ChatHostEvent
import com.r0adkll.livewire.plugin.chat.client.ChatDataConnector
import com.r0adkll.livewire.protocol.SimpleMessage
import com.r0adkll.livewire.ui.icons.ChatBubbleFilled
import com.r0adkll.livewire.ui.icons.ChatBubbleOutline
import com.r0adkll.livewire.ui.icons.HomeFilled
import com.r0adkll.livewire.ui.icons.HomeOutlined
import com.r0adkll.livewire.ui.icons.Send
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val livewireClient: LivewireClient = ServiceLocator.livewireClient

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    livewireClient.start()

    setContent {
      MaterialTheme {
        val scope = rememberCoroutineScope()

        val connectionState by livewireClient.server.connectionState.collectAsState()
        val messages = remember { mutableStateListOf<String>() }

        LaunchedEffect(Unit) {
          livewireClient.server.incomingMessages.collect { envelope ->
            messages.add("$envelope")
          }
        }

        val pagerState = rememberPagerState(2) { 2 }
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                Text(
                  text = "Status: $connectionState",
                )
              }
            )
          },
          bottomBar = {
            NavigationBar {
              NavigationBarItem(
                selected = pagerState.currentPage == 0,
                onClick = {
                  scope.launch { pagerState.animateScrollToPage(0) }
                },
                icon = {
                  Icon(
                    if (pagerState.currentPage == 0) {
                      HomeFilled
                    } else {
                      HomeOutlined
                    },
                    contentDescription = null,
                  )
                },
                label = {
                  Text("Home")
                }
              )
              NavigationBarItem(
                selected = pagerState.currentPage == 1,
                onClick = {
                  scope.launch { pagerState.animateScrollToPage(1) }
                },
                icon = {
                  Icon(
                    if (pagerState.currentPage == 1) {
                      ChatBubbleFilled
                    } else {
                      ChatBubbleOutline
                    },
                    contentDescription = null,
                  )
                },
                label = {
                  Text("Chat")
                }
              )
            }
          }
        ) { padding ->
          HorizontalPager(
            state = pagerState,
            contentPadding = padding,
            modifier = Modifier
              .fillMaxSize()
          ) { page ->
            when (page) {
              0 -> {
                MessagePage(
                  messages = messages,
                  modifier = Modifier.padding(16.dp),
                )
              }

              1 -> {
                val dataConnector = remember {
                  livewireClient.connector<ChatDataConnector>()!!
                }

                ChatPage(
                  dataConnector = dataConnector,
                  modifier = Modifier.padding(16.dp),
                )
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
      stickyHeader {
        Text("Messages", style = MaterialTheme.typography.titleMedium)
      }
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
    Column(
      modifier = modifier.fillMaxSize()
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        stickyHeader {
          val status = if (dataConnector.isConnected) "Connected" else "Disconnected"
          Text("Chat: ${status}", style = MaterialTheme.typography.titleMedium)
        }
        items(dataConnector.messages) { msg ->
          Text(msg, style = MaterialTheme.typography.bodyMedium)
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val messageState = rememberTextFieldState()

        OutlinedTextField(
          state = messageState,
          placeholder = { Text("Enter your message") },
          lineLimits = TextFieldLineLimits.MultiLine(3, 5),
          modifier = Modifier
            .padding(8.dp)
            .weight(1f),
        )

        FilledIconButton(
          enabled = messageState.text.isNotBlank(),
          onClick = {
            val text = messageState.text.toString().trim()
            dataConnector.hostSink(ChatHostEvent.Message(text))
            messageState.clearText()
          }
        ) {
          Icon(
            Send,
            contentDescription = null,
          )
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    livewireClient.stop()
  }
}
