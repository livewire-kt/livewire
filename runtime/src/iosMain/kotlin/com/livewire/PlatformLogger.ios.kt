package com.livewire

import platform.Foundation.NSLog

internal actual fun platformLogDebug(tag: String, message: String) {
  NSLog("[$tag] $message")
}

internal actual fun platformLogError(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    NSLog("[$tag] $message\n$throwable")
  } else {
    NSLog("[$tag] $message")
  }
}
