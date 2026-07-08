package com.livewire.plugin.recomposition

import com.livewire.ui.graphics.toVectorIcon
import kotlin.test.Test
import kotlin.test.assertEquals

class IconsTest {

  @Test
  fun iconsBuildAndRoundTripThroughVectorIcon() {
    listOf(Icons.Compose, Icons.ChevronRight).forEach { icon ->
      assertEquals(icon, icon.toVectorIcon().toImageVector())
    }
  }
}
