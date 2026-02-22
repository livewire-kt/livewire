package com.r0adkll.livewire.client

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.r0adkll.livewire.DataConnector
import com.r0adkll.livewire.client.connector.DataConnectorContent
import com.r0adkll.livewire.protocol.Boundary
import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.DefaultDecoders
import com.r0adkll.livewire.transport.HostEvent
import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

class LivewireClient private constructor(
  val configuration: LivewireClientConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(configure: LivewireClientBuilder.() -> Unit) : this(
    LivewireClientBuilder().apply(configure).build()
  )

  private val scope = CoroutineScope(context + SupervisorJob())

  val server = LivewireServer(
    decoders = configuration.decoders + DefaultDecoders,
  )

  private var activeServerJob: Job? = null

  fun start() {
    server.start()
    activeServerJob = scope.launch {
      launchMolecule(RecompositionMode.Immediate) {
        val connectors = remember {
          mutableStateSetOf<DataConnector<*, *>>()
        }

        LaunchedEffect(Unit) {
          server.incomingMessages.collect { message ->
            Log.d("LivewireClient", "Received message from server: $message")
            when (message) {
              is Boundary.Start -> {
                // Find data connector and push it into service
                val dataConnector = configuration.dataConnectors[message.eventFqName]
                  ?: return@collect

                connectors.add(dataConnector)
              }
              is Boundary.End -> {
                // Remove data connector
                val dataConnector = configuration.dataConnectors[message.eventFqName]
                  ?: return@collect

                connectors.remove(dataConnector)
              }
            }
          }
        }

        connectors.forEach { connector ->
          DataConnectorContent(this@LivewireClient, connector)
        }

      }
    }

    activeServerJob?.invokeOnCompletion {
      server.stop()
    }
  }

  fun stop() {
    activeServerJob?.cancel()
  }

  inline fun <reified T : DataConnector<*, *>> connector(): T? {
    return configuration.dataConnectors.values
      .find { it is T } as? T
  }

  suspend fun ingestMessages(connector: DataConnector<*, *>) {
    val ingestor = configuration.ingestors[connector] ?: error("Plugin ingestor not found! Please check your host configuration")
    ingestor.ingest(server)
  }
}

@LivewireClientDsl
class LivewireClientBuilder {
  val dataConnectors = mutableMapOf<String, DataConnector<*, *>>()
  val ingestors = mutableMapOf<DataConnector<*, *>, PluginEventIngestor<*>>()
  val decoders = mutableSetOf<PayloadDecoder<*>>()

  @Suppress("UNCHECKED_CAST")
  inline fun<reified H : HostEvent, reified C: ClientEvent> install(
    dataConnector: DataConnector<H, C>
  ) {
    dataConnectors[C::class.qualifiedName!!] = dataConnector
    decoders += C::class.companionObjectInstance as PayloadDecoder<C>
    ingestors[dataConnector] = PluginEventIngestor(dataConnector, C::class)
  }


  fun build(): LivewireClientConfiguration {
    return LivewireClientConfiguration(
      dataConnectors = dataConnectors,
      decoders = decoders,
      ingestors = ingestors,
    )
  }
}

class LivewireClientConfiguration(
  val dataConnectors: Map<String, DataConnector<*, *>>,
  val decoders: Set<PayloadDecoder<*>>,
  val ingestors: Map<DataConnector<*, *>, PluginEventIngestor<*>>,
)

@DslMarker
annotation class LivewireClientDsl

class PluginEventIngestor<C : ClientEvent>(
  val connector: DataConnector<*, C>,
  val clientEventClass: KClass<C>,
) {

  suspend fun ingest(server: LivewireServer) {
    server.incomingMessages
      .filterIsInstance(clientEventClass)
      .collect { event ->
        connector.emitEvent(event)
      }
  }
}
