package com.r0adkll.livewire

actual fun logDebug(tag: String, message: String) {
  println("[$tag] $message")
}

actual fun logError(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    println("[$tag] $message\n$throwable")
  } else {
    println("[$tag] $message")
  }
}
