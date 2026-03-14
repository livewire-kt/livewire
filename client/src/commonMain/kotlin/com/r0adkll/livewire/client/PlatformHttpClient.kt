package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val connectionId = Uuid.random().toString()

expect fun createPlatformEngine(): HttpClientEngineFactory<*>
