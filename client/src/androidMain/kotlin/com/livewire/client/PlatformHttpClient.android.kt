package com.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = CIO

actual val supportsWebSocketPings: Boolean = true

