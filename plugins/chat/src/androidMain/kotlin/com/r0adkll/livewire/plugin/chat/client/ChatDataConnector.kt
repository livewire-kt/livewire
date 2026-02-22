package com.r0adkll.livewire.plugin.chat.client

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.r0adkll.livewire.DataConnector
import com.r0adkll.livewire.plugin.chat.ChatClientEvent
import com.r0adkll.livewire.plugin.chat.ChatHostEvent

class ChatDataConnector : DataConnector<ChatHostEvent, ChatClientEvent>() {

  val messages = mutableStateListOf<String>()

  @Composable
  override fun Connect() {
    onEvent { event ->
      when (event) {
        is ChatClientEvent.Message -> {
          messages.add(event.msg)
          Log.d("ChatClient", "Message[${messages.size}] ${event.msg}")
        }
      }
    }
  }
}
