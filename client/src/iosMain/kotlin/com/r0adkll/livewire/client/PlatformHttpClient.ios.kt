package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = Darwin
