package com.r0adkll.livewire.plugin.recomposition

import kotlin.test.Test
import kotlin.test.assertEquals

class BoundedQueueTest {
  @Test
  fun `evicts the oldest elements after maximum capacity`() {
    val q = BoundedQueue<Int>(3)
    listOf(1, 2, 3, 4, 5).forEach(q::add)
    assertEquals(listOf(3, 4, 5), q.toList())
  }
}
