package com.r0adkll.livewire.client

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSProcessInfo

actual fun createPlatformEngine(): HttpClientEngineFactory<*> = Darwin
actual fun deviceIdFilter(): String? = NSProcessInfo.processInfo.environment["SIMULATOR_UDID"] as? String
