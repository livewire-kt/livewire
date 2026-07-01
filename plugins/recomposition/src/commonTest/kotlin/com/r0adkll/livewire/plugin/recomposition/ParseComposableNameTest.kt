@file:OptIn(ComposeToolingApi::class)

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.parseSourceInformation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParseComposableNameTest {
  @Test
  fun `returns null when source information is null`() {
    assertNull(parseComposableName(null))
  }

  @Test
  fun `keeps the name of a regular composable`() {
    assertEquals("Button", nameOf("C(Button)"))
  }

  @Test
  fun `filters out non-Unit returning composables`() {
    assertNull(nameOf("C(rememberFoo)"))
  }

  @Test
  fun `filters out effects`() {
    assertNull(nameOf("C(LaunchedEffect)"))
    assertNull(nameOf("C(DisposableEffect)"))
  }

  @Test
  fun `filters out providers`() {
    assertNull(nameOf("C(ProvideStuff)"))
    assertNull(nameOf("C(ThemeProvider)"))
  }

  @Test
  fun `filters out internal helpers`() {
    assertNull(nameOf("C(SkippableItem)"))
    assertNull(nameOf("C(ReusableComposeNode)"))
    assertNull(nameOf("C(ReusableContent)"))
  }
}

private fun nameOf(sourceInfo: String): String? = parseComposableName(parseSourceInformation(sourceInfo))
