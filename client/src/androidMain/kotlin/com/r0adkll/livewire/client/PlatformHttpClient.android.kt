package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = OkHttp
