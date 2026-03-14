package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = CIO
