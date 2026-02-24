package com.r0adkll.livewire.ui.modifier.mapping

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Mapping layer for modifiers to convert their serialized data
 * model format, [com.r0adkll.livewire.ui.modifier.LivewireModifier] to the
 * Compose Ui format, [Modifier]
 */
@Suppress("ModifierFactoryExtensionFunction")
interface ComposeUiMapper {

  @Composable
  fun toComposeUi(then: Modifier): Modifier = then

  @Composable
  fun ColumnScope.toComposeUi(then: Modifier): Modifier = this@ComposeUiMapper.toComposeUi(then)

  @Composable
  fun RowScope.toComposeUi(then: Modifier): Modifier = this@ComposeUiMapper.toComposeUi(then)

  @Composable
  fun BoxScope.toComposeUi(then: Modifier): Modifier = this@ComposeUiMapper.toComposeUi(then)
}
