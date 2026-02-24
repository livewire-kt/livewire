package com.r0adkll.livewire.ui.modifier


import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.r0adkll.livewire.ui.modifier.DimensionModifier.Type
import com.r0adkll.livewire.ui.modifier.LivewireModifier.Element.Companion.BOUNDARY
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import okio.BufferedSink

@Serializable
internal class WidthModifier(
  val type: Type,
  val value: Float,
) : LivewireModifier.Element {

  @Composable
  override fun Modifier.toComposeUi(): Modifier {
    return when (type) {
      Type.FILL -> this.fillMaxWidth(value)
      Type.WRAP -> this.wrapContentWidth()
      Type.INTRINSIC_MIN -> this.width(IntrinsicSize.Min)
      Type.INTRINSIC_MAX -> this.width(IntrinsicSize.Max)
      Type.EXACT_DP -> this.width(Dp(value))
      else -> this
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
