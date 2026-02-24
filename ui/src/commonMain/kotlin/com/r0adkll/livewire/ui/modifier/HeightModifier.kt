package com.r0adkll.livewire.ui.modifier

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.r0adkll.livewire.ui.modifier.DimensionModifier.Type
import kotlinx.serialization.Serializable

@Serializable
internal class HeightModifier(
  val type: Type,
  val value: Float
) : LivewireModifier.Element {

  @Composable
  override fun Modifier.toComposeUi(): Modifier {
    return when (type) {
      Type.FILL -> this.fillMaxHeight(value)
      Type.WRAP -> this.wrapContentHeight()
      Type.INTRINSIC_MIN -> this.height(IntrinsicSize.Min)
      Type.INTRINSIC_MAX -> this.height(IntrinsicSize.Max)
      Type.EXACT_DP -> this.height(Dp(value))
      else -> this
    }
  }

}

fun LivewireModifier.height(height: Dp): LivewireModifier =
  then(HeightModifier(Type.EXACT_DP, height.value))

fun LivewireModifier.fillMaxHeight(fraction: Float = 1f): LivewireModifier =
  then(HeightModifier(Type.FILL, fraction))

fun LivewireModifier.height(height: IntrinsicSize): LivewireModifier {
  return if (height == IntrinsicSize.Min) {
    then(HeightModifier(Type.INTRINSIC_MIN, 0f))
  } else {
    then(HeightModifier(Type.INTRINSIC_MAX, 0f))
  }
}
