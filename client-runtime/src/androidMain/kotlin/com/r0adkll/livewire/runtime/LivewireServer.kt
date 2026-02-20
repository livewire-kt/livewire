package com.r0adkll.livewire.runtime

import com.r0adkll.livewire.livewire.LIVEWIRE_PORT
import com.r0adkll.livewire.livewire.LIVEWIRE_WS_PATH
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectionState {
    STOPPED,
    STARTED,
    CONNECTED,
    ERROR,
}

class LivewireServer {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow(ConnectionState.STOPPED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages.asSharedFlow()

    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null
    private var activeSession: WebSocketSession? = null

    fun start() {
        server = embeddedServer(CIO, port = LIVEWIRE_PORT) {
            install(WebSockets)
            routing {
                webSocket(LIVEWIRE_WS_PATH) {
                    activeSession = this
                    _connectionState.value = ConnectionState.CONNECTED
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                _incomingMessages.tryEmit(text)
                                // Echo back for initial testing
                                send(Frame.Text("$text -> pong"))
                            }
                        }
                    } finally {
                        activeSession = null
                        _connectionState.value = ConnectionState.STARTED
                    }
                }
            }
        }.also {
            it.start(wait = false)
            _connectionState.value = ConnectionState.STARTED
        }
    }

    suspend fun send(message: String) {
        activeSession?.send(Frame.Text(message))
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
        activeSession = null
        _connectionState.value = ConnectionState.STOPPED
        scope.cancel()
    }
}
