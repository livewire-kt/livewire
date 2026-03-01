package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.transport.DefaultDecoders
import com.r0adkll.livewire.transport.PayloadDecoder
import com.r0adkll.livewire.ui.actions.LivewireAction
import com.r0adkll.livewire.ui.actions.LivewireActionDispatcher
import com.r0adkll.livewire.ui.data.UiDecoders
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalSerializationApi::class)
class LivewireHost private constructor(
  val configuration: LivewireHostConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) : LivewireActionDispatcher {

  constructor(configure: LivewireHostBuilder.() -> Unit = {}) : this(
    LivewireHostBuilder().apply(configure).build()
  )

  val connection = LivewireHostConnection(
    decoders = configuration.decoders + DefaultDecoders + UiDecoders,
    context = context,
  )

  override suspend fun dispatch(action: LivewireAction) {
    connection.send(action)
  }
}

@LivewireHostDsl
class LivewireHostBuilder  {
  val decoders = mutableSetOf<PayloadDecoder<*>>()

  fun build(): LivewireHostConfiguration {
    return LivewireHostConfiguration(
      decoders = decoders,
    )
  }
}

class LivewireHostConfiguration(
  val decoders: Set<PayloadDecoder<*>>,
)

@DslMarker
annotation class LivewireHostDsl
