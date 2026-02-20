package com.r0adkll.livewire.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AdbForwarder {

    suspend fun forward(port: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder("adb", "forward", "tcp:$port", "tcp:$port")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (exitCode != 0) {
                error("adb forward failed (exit $exitCode): $output")
            }
        }
    }

    suspend fun removeForward(port: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder("adb", "forward", "--remove", "tcp:$port")
                .redirectErrorStream(true)
                .start()
            process.waitFor()
            Unit
        }
    }
}
