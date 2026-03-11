package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory

expect fun createPlatformEngine(): HttpClientEngineFactory<*>
expect fun simulatorId(): String?
