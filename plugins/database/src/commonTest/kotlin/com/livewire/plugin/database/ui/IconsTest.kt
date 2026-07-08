package com.livewire.plugin.database.ui

import com.livewire.ui.graphics.toVectorIcon
import kotlin.test.Test
import kotlin.test.assertEquals

class IconsTest {

  @Test
  fun iconsBuildAndRoundTripThroughVectorIcon() {
    listOf(
      Icons.Database,
      Icons.Table,
      Icons.DropdownArrow,
      Icons.DatabaseSearch,
      Icons.Run,
      Icons.Schema,
    ).forEach { icon ->
      assertEquals(icon, icon.toVectorIcon().toImageVector())
    }
  }
}
