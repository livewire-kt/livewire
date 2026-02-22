package com.r0adkll.livewire.transport.events

import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent

sealed interface DatabaseHostEvent : HostEvent {
  data class DatabaseList(val names: List<String>) : DatabaseHostEvent
  data class TableList(val names: List<String>) : DatabaseHostEvent
  data class TableSchema(val names: List<List<String>>) : DatabaseHostEvent
  data class QueryResults(val rows: List<List<String>>) : DatabaseHostEvent
}

sealed interface DatabaseClientEvent : ClientEvent {
  data object GetDatabases : DatabaseClientEvent
  data class GetTables(val database: String) : DatabaseClientEvent
  data class GetTableSchema(val database: String, val tableName: String) : DatabaseClientEvent
  data class Query(val database: String, val query: String) : DatabaseClientEvent
}