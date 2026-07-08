package com.livewire.plugin.playground

import com.livewire.ui.graphics.toVectorIcon
import kotlin.test.Test
import kotlin.test.assertEquals

class IconsTest {

  @Test
  fun iconsBuildAndRoundTripThroughVectorIcon() {
    listOf(Icons.Playground, Icons.Sync).forEach { icon ->
      assertEquals(icon, icon.toVectorIcon().toImageVector())
    }
  }
}
