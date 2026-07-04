package com.livewire.ui.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.livewire.transport.PayloadDecoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
sealed interface LivewireAction {

  companion object : PayloadDecoder<LivewireAction> {
    override fun Json.decodePayload(element: JsonElement): LivewireAction = decodeFromJsonElement(serializer(), element)
  }
}

interface LivewireActionObserver {
  val events: Flow<LivewireAction>

  companion object NoOp : LivewireActionObserver {
    override val events: Flow<LivewireAction> = emptyFlow()
  }
}

val LocalLivewireActionObserver = staticCompositionLocalOf<LivewireActionObserver> {
  error("LivewireActionObserver not initialized")
}

interface LivewireActionDispatcher {
  suspend fun dispatch(action: LivewireAction)
}

val LocalLivewireActionDispatcher = staticCompositionLocalOf<LivewireActionDispatcher> {
  error("LivewireActionDispatcher not initialized")
}

class LivewireActionController
internal constructor(): LivewireActionDispatcher, LivewireActionObserver {

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
fun rememberLivewireActionController() : LivewireActionController {
  return remember {
    LivewireActionController()
  }
}
