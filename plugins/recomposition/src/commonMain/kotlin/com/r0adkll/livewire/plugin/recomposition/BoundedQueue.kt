package com.r0adkll.livewire.plugin.recomposition

import kotlin.concurrent.Volatile

internal class BoundedQueue<T : Any>(private val maxSize: Int) {
  @Suppress("UNCHECKED_CAST")
  private val ringBuffer: Array<T?> = arrayOfNulls<Any>(maxSize) as Array<T?>

  @Volatile
  private var written = 0L

  fun add(item: T) {
    ringBuffer[(written % maxSize).toInt()] = item
    written += 1
  }

  @Suppress("UNCHECKED_CAST")
  fun toList(): List<T> {
    val total = written
    val size = minOf(total, maxSize.toLong()).toInt()
    val start = total - size
    return List(size) { ringBuffer[((start + it) % maxSize).toInt()] as T }
  }
}
