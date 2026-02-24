package com.r0adkll.livewire.ui.modifier


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable
internal class PaddingModifier(
  val start: Float,
  val top: Float,
  val end: Float,
  val bottom: Float,
) : LivewireModifier.Element {
  init {
    require(
      (start >= 0f) and
        (top >= 0f) and
        (end >= 0f) and
        (bottom >= 0f)
    ) {
      "Padding must be non-negative"
    }
  }

  @Composable
  override fun Modifier.toComposeUi(): Modifier {
    return this.padding(
      start = start.dp,
      end = end.dp,
      top = top.dp,
      bottom = bottom.dp,
    )
  }

}

/** Adds padding defined by the [padding] object. */
fun LivewireModifier.padding(padding: PaddingValues): LivewireModifier =
  padding(
    left = padding.calculateLeftPadding(LayoutDirection.Ltr),
    top = padding.calculateTopPadding(),
    right = padding.calculateRightPadding(LayoutDirection.Ltr),
    bottom = padding.calculateTopPadding(),
  )

fun LivewireModifier.padding(all: Dp): LivewireModifier =
  padding(left = all, top = all, right = all, bottom = all)

/**
 * Adds padding to each edge of the content using [Dp] values.
 *
 * @param left Padding at the left edge.
 * @param top Padding at the top edge.
 * @param right Padding at the right edge.
 * @param bottom Padding at the bottom edge.
 */
fun LivewireModifier.padding(
  left: Dp = 0.dp,
  top: Dp = 0.dp,
  right: Dp = 0.dp,
  bottom: Dp = 0.dp,
): LivewireModifier {
  return then(PaddingModifier(left.value, top.value, right.value, bottom.value))
}

/**
 * Adds [horizontal] padding to the left and right edges, and [vertical] padding to the top and
 * bottom edges.
 */
fun LivewireModifier.padding(
  horizontal: Dp = 0.dp,
  vertical: Dp = 0.dp,
): LivewireModifier =
  padding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)
