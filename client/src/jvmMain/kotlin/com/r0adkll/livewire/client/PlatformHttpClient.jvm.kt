package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val stableDeviceId = Uuid.random().toString()

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = CIO
actual fun deviceIdFilter(): String? = stableDeviceId
