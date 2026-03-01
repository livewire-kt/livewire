package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.layout.Alignment
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class AlignModifier(
  val alignment: Alignment,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    // align is scope-dependent; outside a scope, pass through
    return then
  }

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun BoxScope.toComposeUi(then: Modifier): Modifier {
    val composeAlignment = when (alignment) {
      Alignment.TopStart -> androidx.compose.ui.Alignment.TopStart
      Alignment.TopCenter -> androidx.compose.ui.Alignment.TopCenter
      Alignment.TopEnd -> androidx.compose.ui.Alignment.TopEnd
      Alignment.CenterStart -> androidx.compose.ui.Alignment.CenterStart
      Alignment.Center -> androidx.compose.ui.Alignment.Center
      Alignment.CenterEnd -> androidx.compose.ui.Alignment.CenterEnd
      Alignment.BottomStart -> androidx.compose.ui.Alignment.BottomStart
      Alignment.BottomCenter -> androidx.compose.ui.Alignment.BottomCenter
      Alignment.BottomEnd -> androidx.compose.ui.Alignment.BottomEnd
      // Vertical/Horizontal subtypes used in a Box context — fall back to sensible defaults
      Alignment.Top -> androidx.compose.ui.Alignment.TopCenter
      Alignment.CenterVertically -> androidx.compose.ui.Alignment.Center
      Alignment.Bottom -> androidx.compose.ui.Alignment.BottomCenter
      Alignment.Start -> androidx.compose.ui.Alignment.CenterStart
      Alignment.CenterHorizontally -> androidx.compose.ui.Alignment.Center
      Alignment.End -> androidx.compose.ui.Alignment.CenterEnd
    }
    return then.align(composeAlignment)
  }

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun RowScope.toComposeUi(then: Modifier): Modifier {
    val composeAlignment = when (alignment) {
      Alignment.Top -> androidx.compose.ui.Alignment.Top
      Alignment.CenterVertically -> androidx.compose.ui.Alignment.CenterVertically
      Alignment.Bottom -> androidx.compose.ui.Alignment.Bottom
      // 2D alignments — extract vertical component
      Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> androidx.compose.ui.Alignment.Top
      Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> androidx.compose.ui.Alignment.CenterVertically
      Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> androidx.compose.ui.Alignment.Bottom
      // Horizontal subtypes in RowScope — default to center vertically
      Alignment.Start, Alignment.CenterHorizontally, Alignment.End -> androidx.compose.ui.Alignment.CenterVertically
    }
    return then.align(composeAlignment)
  }

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun ColumnScope.toComposeUi(then: Modifier): Modifier {
    val composeAlignment = when (alignment) {
      Alignment.Start -> androidx.compose.ui.Alignment.Start
      Alignment.CenterHorizontally -> androidx.compose.ui.Alignment.CenterHorizontally
      Alignment.End -> androidx.compose.ui.Alignment.End
      // 2D alignments — extract horizontal component
      Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> androidx.compose.ui.Alignment.Start
      Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> androidx.compose.ui.Alignment.CenterHorizontally
      Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> androidx.compose.ui.Alignment.End
      // Vertical subtypes in ColumnScope — default to center horizontally
      Alignment.Top, Alignment.CenterVertically, Alignment.Bottom -> androidx.compose.ui.Alignment.CenterHorizontally
    }
    return then.align(composeAlignment)
  }
}
