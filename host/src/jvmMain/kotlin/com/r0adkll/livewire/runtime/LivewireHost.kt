package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.HostPlugin
import com.r0adkll.livewire.protocol.Boundary
import com.r0adkll.livewire.protocol.EnvelopeJson
import com.r0adkll.livewire.protocol.SimpleMessage
import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent
import com.r0adkll.livewire.transport.PayloadDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

class LivewireHost private constructor(
  val configuration: LivewireHostConfiguration,
  context: CoroutineContext = Dispatchers.IO,
) {

  constructor(configure: LivewireHostBuilder.() -> Unit) : this(
    LivewireHostBuilder().apply(configure).build()
  )

  private val scope = CoroutineScope(context + SupervisorJob())

  val connection = LivewireHostConnection(
    decoders = configuration.hostEventDecoders + SimpleMessage + Boundary,
    context = context,
  )

  fun startDataConnection(plugin: HostPlugin<*, *>) {
    val clientEventFqName = configuration.clientEventFqNames[plugin] ?: error("Client event $plugin not found")
    scope.launch {
      val boundary: Boundary = Boundary.Start(clientEventFqName)
      connection.send(boundary)
    }
  }

  fun stopDataConnection(plugin: HostPlugin<*, *>) {
    val clientEventFqName = configuration.clientEventFqNames[plugin] ?: error("Client event $plugin not found")
    scope.launch {
      val boundary: Boundary = Boundary.End(clientEventFqName)
      connection.send(boundary)
    }
  }

  suspend fun ingestMessages(plugin: HostPlugin<*, *>) {
    val ingestor = configuration.ingestors[plugin] ?: error("Plugin ingestor not found! Please check your host configuration")
    ingestor.ingest(connection)
  }

  @Suppress("UNCHECKED_CAST")
  suspend fun <C : ClientEvent> send(plugin: HostPlugin<*, *>, event: C) {
    val clientEventSink = configuration.clientEventSinks[plugin] as? PluginClientEventSink<C>
      ?: error("Client event $plugin not found")
    clientEventSink.send(connection, event)
  }
}

@LivewireHostDsl
class LivewireHostBuilder  {
  val decoders = mutableSetOf<PayloadDecoder<*>>()
  val ingestors = mutableMapOf<HostPlugin<*, *>, PluginEventIngestor<*>>()
  val clientEventSinks = mutableMapOf<HostPlugin<*, *>, PluginClientEventSink<*>>()
  val plugins = mutableSetOf<HostPlugin<*, *>>()
  val hostEventFqNames = mutableMapOf<HostPlugin<*, *>, String>()
  val clientEventFqNames = mutableMapOf<HostPlugin<*, *>, String>()

  @Suppress("UNCHECKED_CAST")
  inline fun <reified H : HostEvent, reified C : ClientEvent> install(
    plugin: HostPlugin<H, C>,
  ) {
    hostEventFqNames[plugin] = H::class.qualifiedName!!
    clientEventFqNames[plugin] = C::class.qualifiedName!!
    decoders += H::class.companionObjectInstance as PayloadDecoder<H>
    plugins += plugin
    ingestors[plugin] = PluginEventIngestor(plugin, H::class)

    val clientEventDecoder = C::class.companionObjectInstance as PayloadDecoder<C>
    clientEventSinks[plugin] = PluginClientEventSink(clientEventDecoder)
  }

  fun build(): LivewireHostConfiguration {
    return LivewireHostConfiguration(
      hostEventFqNames = hostEventFqNames,
      clientEventFqNames = clientEventFqNames,
      hostEventDecoders = decoders,
      ingestors = ingestors,
      clientEventSinks = clientEventSinks,
      plugins = plugins
    )
  }
}

class LivewireHostConfiguration(
  val hostEventFqNames: Map<HostPlugin<*, *>, String>,
  val clientEventFqNames: Map<HostPlugin<*, *>, String>,
  val hostEventDecoders: Set<PayloadDecoder<*>>,
  val ingestors: Map<HostPlugin<*, *>, PluginEventIngestor<*>>,
  val clientEventSinks: Map<HostPlugin<*, *>, PluginClientEventSink<*>>,
  val plugins: Set<HostPlugin<*, *>>,
)

@DslMarker
annotation class LivewireHostDsl

class PluginEventIngestor<H : HostEvent>(
  val plugin: HostPlugin<H, *>,
  val hostEventClass: KClass<H>,
) {

  suspend fun ingest(connection: LivewireHostConnection) {
    connection.incomingMessages
      .filterIsInstance(hostEventClass)
      .collect { event ->
        plugin.emitEvent(event)
      }
  }
}

class PluginClientEventSink<C : ClientEvent>(
  val payloadDecoder: PayloadDecoder<C>,
) {

  suspend fun send(connection: LivewireHostConnection, event: C) {
    val eventJson = EnvelopeJson.encodeToString(payloadDecoder.serializer(), event)
    println("Send: $eventJson")
    connection.sendRaw(eventJson)
  }
}
