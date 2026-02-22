package com.r0adkll.livewire.plugin.host

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.HostPlugin
import com.r0adkll.livewire.plugin.chat.ChatClientEvent
import com.r0adkll.livewire.plugin.chat.ChatHostEvent
import com.r0adkll.livewire.plugin.host.composables.icons.Chat
import com.r0adkll.livewire.plugin.host.composables.icons.Send

class ChatHostPlugin : HostPlugin<ChatHostEvent, ChatClientEvent>() {

  @Composable
  override fun createPresentation(): Presentation {
    return Presentation(
      icon = Chat,
      title = "Chat",
    )
  }

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  @Composable
  override fun Content(modifier: Modifier) {
    val messages = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
      hostEvents.collect { event ->
        when (event) {
          is ChatHostEvent.Message -> messages.add(event.message)
        }
      }
    }

    Column {
      LazyColumn(
        modifier = modifier.weight(1f),
      ) {
        stickyHeader {
          Text(
            text = "Chat",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }

        items(messages) { message ->
          Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 8.dp)
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val messageState = rememberTextFieldState()

        OutlinedTextField(
          state = messageState,
          placeholder = { Text("Enter your message") },
          lineLimits = TextFieldLineLimits.MultiLine(3, 10),
          modifier = Modifier
            .padding(8.dp)
            .weight(1f),
        )

        val sendButtonHeight = ButtonDefaults.MinHeight
        Button(
          onClick = {
            val messageText = messageState.text.trim()
              .ifBlank { null }
              ?.toString()
            if (messageText != null) {
              clientSink(ChatClientEvent.Message(messageText))
              messageState.clearText()
            }
          },
          shapes = ButtonDefaults.shapes(),
          modifier = Modifier
            .padding(
              start = 8.dp,
              end = 16.dp,
            )
            .heightIn(sendButtonHeight),
        ) {
          Icon(
            Send,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(sendButtonHeight))
          )
          Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(sendButtonHeight)))
          Text(
            text = "Send",
            style = ButtonDefaults.textStyleFor(sendButtonHeight),
          )
        }
      }
    }
  }
}
