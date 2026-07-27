package com.livewire.runtime

sealed interface ConnectionError {
  val message: String

  data class PortInUse(val port: Int) : ConnectionError {
    override val message = "One or more ports required by Livewire is in use by another app. Quit the other app and restart Livewire."
  }

  data class ConnectionFailed(
    val appName: String?,
    val cause: String,
  ) : ConnectionError {
    override val message = if (appName == null) "Couldn't connect: $cause" else "Couldn't connect to $appName: $cause"
  }
}
