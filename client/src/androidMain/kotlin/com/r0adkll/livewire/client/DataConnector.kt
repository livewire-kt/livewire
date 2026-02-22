package com.r0adkll.livewire.client

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.r0adkll.livewire.DataConnector
import com.r0adkll.livewire.transport.ClientEvent
import com.r0adkll.livewire.transport.HostEvent
import com.r0adkll.livewire.transport.events.DatabaseClientEvent
import com.r0adkll.livewire.transport.events.DatabaseHostEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch


// TODO: This is just a sample to explore API / DSLs

/*
 * Client side configuration for 0
 */

interface DatabaseExplorer {
  fun buildConnections()
  fun closeConnections()

  suspend fun findAllDatabases(): List<String>
  suspend fun getTables(database: String): List<String>
  suspend fun getTableSchema(database: String, table: String): List<List<String>>
  suspend fun query(database: String, query: String): List<List<String>>
}

class RealDatabaseExplorer(
  private val context: Context,
) : DatabaseExplorer {
  override fun buildConnections() {
    TODO("Not yet implemented")
  }

  override fun closeConnections() {
    TODO("Not yet implemented")
  }

  override suspend fun findAllDatabases(): List<String> {
    TODO("Not yet implemented")
  }

  override suspend fun getTables(database: String): List<String> {
    TODO("Not yet implemented")
  }

  override suspend fun getTableSchema(
    database: String,
    table: String
  ): List<List<String>> {
    TODO("Not yet implemented")
  }

  override suspend fun query(
    database: String,
    query: String
  ): List<List<String>> {
    TODO("Not yet implemented")
  }
}


class DatabaseDataConnector(
  private val db: DatabaseExplorer,
) : DataConnector<DatabaseHostEvent, DatabaseClientEvent>() {

  constructor(context: Context) : this(RealDatabaseExplorer(context))

  @Composable
  override fun Connect() {
    val scope = rememberCoroutineScope()

    // Composition starts when the plugin is in-focus in the host
    // app and exits when connection is lost or host plugin is unloaded
    DisposableEffect(Unit) {
      db.buildConnections()
      onDispose {
        db.closeConnections()
      }
    }

    // Eagerly output all available database names upon connection
    LaunchedEffect(Unit) {
      val databases = db.findAllDatabases()
      hostSink(DatabaseHostEvent.DatabaseList(databases))
      // TODO: Maybe prefetch table lists too?
    }

    // Listen for any events sent by host applicatidon, and respond
    onEvent { event ->
      when (event) {
        DatabaseClientEvent.GetDatabases -> scope.launch {
          val databases = db.findAllDatabases()
          hostSink(DatabaseHostEvent.DatabaseList(databases))
        }
        is DatabaseClientEvent.GetTables -> scope.launch {
          val tables = db.getTables(event.database)
          hostSink(DatabaseHostEvent.TableList(tables))
        }
        is DatabaseClientEvent.GetTableSchema -> scope.launch {
          val schema = db.getTableSchema(event.database, event.tableName)
          hostSink(DatabaseHostEvent.TableSchema(schema))
        }
        is DatabaseClientEvent.Query -> scope.launch {
          val results = db.query(event.database, event.query)
          hostSink(DatabaseHostEvent.QueryResults(results))
        }
      }
    }
  }
}
