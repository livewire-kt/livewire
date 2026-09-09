package com.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val connectionId = Uuid.random().toString()

expect fun createPlatformEngine(): HttpClientEngineFactory<*>

/**
 * Whether the platform's WebSocket transport supports sending protocol-level ping frames.
 * Browsers don't expose ping/pong to page code, so web clients rely on host-side pings.
 */
expect val supportsWebSocketPings: Boolean
