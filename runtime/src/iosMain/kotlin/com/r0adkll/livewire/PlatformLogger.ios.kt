package com.r0adkll.livewire

import platform.Foundation.NSLog

actual fun logDebug(tag: String, message: String) {
  NSLog("[$tag] $message")
}

actual fun logError(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    NSLog("[$tag] $message\n$throwable")
  } else {
    NSLog("[$tag] $message")
  }
}
