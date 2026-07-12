package com.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = Js

actual val supportsWebSocketPings: Boolean = false

