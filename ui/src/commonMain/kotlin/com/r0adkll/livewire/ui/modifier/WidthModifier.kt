package com.r0adkll.livewire.ui.modifier


import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.r0adkll.livewire.ui.modifier.DimensionModifier.Type
import kotlinx.serialization.Serializable

@Suppress("ModifierFactoryExtensionFunction")
@Serializable
internal class WidthModifier(
  val type: Type,
  val value: Float,
) : LivewireModifier.Element {

  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    return when (type) {
      Type.FILL -> then.fillMaxWidth(value)
      Type.WRAP -> then.wrapContentWidth()
      Type.INTRINSIC_MIN -> then.width(IntrinsicSize.Min)
      Type.INTRINSIC_MAX -> then.width(IntrinsicSize.Max)
      Type.EXACT_DP -> then.width(Dp(value))
      else -> then
    }
  }
  @Composable
  override fun RowScope.toComposeUi(then: Modifier): Modifier {
    return when (type) {
      Type.WEIGHT -> then.weight(value)
      else -> this@WidthModifier.toComposeUi(then)
    }
  }
}

fun LivewireModifier.width(width: Dp): LivewireModifier =
  then(WidthModifier(Type.EXACT_DP, width.value))

fun LivewireModifier.fillMaxWidth(fraction: Float = 1f): LivewireModifier =
  then(WidthModifier(Type.FILL, fraction))

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun LivewireModifier.width(width: IntrinsicSize): LivewireModifier {
  return if (width == IntrinsicSize.Min) {
    then(WidthModifier(Type.INTRINSIC_MIN, 0f))
  } else {
    then(WidthModifier(Type.INTRINSIC_MAX, 0f))
  }
}
