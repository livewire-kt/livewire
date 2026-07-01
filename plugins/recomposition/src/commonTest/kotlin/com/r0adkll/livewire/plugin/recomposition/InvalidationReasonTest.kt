package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow

class InvalidationReasonTest {
  @Test
  fun `exposes no value for an unknown invalidation reason`() {
    val reason = InvalidationReason.Unknown()
    assertEquals(InvalidationReason.Unknown().label, reason.label)
    assertNull(reason.value)
  }

  @Test
  fun `exposes no value for a direct invalidation reason`() {
    val reason = InvalidationReason.Direct()
    assertEquals(InvalidationReason.Direct().label, reason.label)
    assertNull(reason.value)
  }

  @Test
  fun `reads the current value when the invalidation reason is a state object`() {
    val reason = InvalidationReason.Reason(mutableStateOf(7))
    assertEquals("7", reason.value)
    assertTrue(reason.label.startsWith("Mutable"))
  }

  @Test
  fun `shows the size when the invalidation reason is a collection`() {
    assertTrue(InvalidationReason.Reason(listOf(1, 2)).value!!.contains("size=2"))
  }

  @Test
  fun `labels the invalidation reason as StateFlow`() {
    assertEquals("StateFlow", InvalidationReason.Reason(MutableStateFlow(1)).label)
  }

  @Test
  fun `labels the invalidation reason as SnapshotStateList`() {
    assertEquals("SnapshotStateList", InvalidationReason.Reason(mutableStateListOf(1, 2)).label)
  }

  @Test
  fun `labels the invalidation reason as SnapshotStateMap`() {
    assertEquals("SnapshotStateMap", InvalidationReason.Reason(mutableStateMapOf(1 to 2)).label)
  }
}
