package com.r0adkll.livewire.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Immutable
@Serializable
sealed interface LivewireAction {

  companion object : PayloadDecoder<LivewireAction> {
    override suspend fun Json.decodePayload(rawPayload: String): LivewireAction {
      return decodeFromString(serializer(), rawPayload)
    }
  }
}

val LocalLivewireActionObserver = staticCompositionLocalOf<LivewireActionObserver> {
  error("LivewireActionObserver not initialized")
}

interface LivewireActionObserver {
  val events: Flow<LivewireAction>
}

interface LivewireActionController : LivewireActionObserver {
  suspend fun dispatch(action: LivewireAction)
}

private class LivewireActionControllerImpl : LivewireActionController {
  private val _events = MutableSharedFlow<LivewireAction>(
    replay = 0,
    extraBufferCapacity = 20,
  )
  override val events get() = _events.asSharedFlow()

  override suspend fun dispatch(action: LivewireAction) {
    _events.emit(action)
  }
}

@Composable
fun rememberLivewireActionController() : LivewireActionController{
  return remember {
    LivewireActionControllerImpl()
  }
}
