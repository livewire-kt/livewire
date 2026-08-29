package com.livewire

internal actual fun platformLogDebug(tag: String, message: String) {
  println("[$tag] $message")
}

internal actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    println("[$tag] $message\n$throwable")
  } else {
    println("[$tag] $message")
  }
}
