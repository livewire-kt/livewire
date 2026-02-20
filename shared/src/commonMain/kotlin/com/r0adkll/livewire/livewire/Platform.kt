package com.r0adkll.livewire.livewire

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform