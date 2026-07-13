package com.livewire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.InternalComposeApi
import com.livewire.client.LivewireClient

class MainActivity : ComponentActivity() {

  private val livewireClient: LivewireClient = ServiceLocator.livewireClient

  @OptIn(InternalComposeApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    setContent {
      MaterialTheme {
        LivewireApp(
          livewireClient = livewireClient,
          settings = ServiceLocator.settingsDataStore,
        )
      }
    }
  }
}
