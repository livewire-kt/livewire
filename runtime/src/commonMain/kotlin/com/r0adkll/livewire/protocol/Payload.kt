package com.r0adkll.livewire.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
open class Payload(open val type: String) {

  override fun toString(): String {
    return "Payload(type='$type')"
  }
}
