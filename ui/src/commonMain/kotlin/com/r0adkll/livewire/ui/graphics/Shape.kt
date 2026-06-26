package com.r0adkll.livewire.ui.graphics

import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape as ComposeCircleShape
import androidx.compose.foundation.shape.RoundedCornerShape as ComposeRoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape as ComposeRectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import androidx.compose.foundation.shape.CornerSize as ComposeCornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape as ComposeShape

@Immutable
@Serializable
sealed interface Shape {
  fun toComposeUi(): ComposeShape
}

@Immutable
@Serializable
data object RectangleShape : Shape {
  override fun toComposeUi(): ComposeShape {
    return ComposeRectangleShape
  }
}

@Immutable
@Serializable
data object CircleShape : Shape {
  override fun toComposeUi(): ComposeShape = ComposeCircleShape
}

@Immutable
@Serializable
data class RoundedCornerShape(
  val topStart: CornerSize,
  val topEnd: CornerSize,
  val bottomStart: CornerSize,
  val bottomEnd: CornerSize,
) : Shape {
  constructor(all: CornerSize) : this(all, all, all, all)

  override fun toComposeUi(): ComposeShape = ComposeRoundedCornerShape(
    topStart = topStart.toComposeUi(),
    topEnd = topEnd.toComposeUi(),
    bottomStart = bottomStart.toComposeUi(),
    bottomEnd = bottomEnd.toComposeUi(),
  )

}

fun RoundedCornerShape(size: Dp) = RoundedCornerShape(CornerSize(size))
fun RoundedCornerShape(
  topStart: Dp,
  topEnd: Dp,
  bottomStart: Dp,
  bottomEnd: Dp,
) = RoundedCornerShape(
  topStart = CornerSize(topStart),
  topEnd = CornerSize(topEnd),
  bottomStart = CornerSize(bottomStart),
  bottomEnd = CornerSize(bottomEnd),
)

fun RoundedCornerShape(pixels: Float) = RoundedCornerShape(CornerSize(pixels))
fun RoundedCornerShape(percentage: Int) = RoundedCornerShape(CornerSize(percentage))

@Immutable
@Serializable
data class CornerSize(
  val value: Float,
  val type: Type,
) {

  @Serializable
  enum class Type {
    Dp,
    Px,
    Percent,
  }

  fun toComposeUi(): ComposeCornerSize = when (type) {
    Type.Dp -> ComposeCornerSize(value.dp)
    Type.Px -> ComposeCornerSize(value)
    Type.Percent -> ComposeCornerSize(value.toInt())
  }

}

fun CornerSize(size: Dp): CornerSize = CornerSize(size.value, CornerSize.Type.Dp)
fun CornerSize(pixels: Float): CornerSize = CornerSize(pixels, CornerSize.Type.Px)
fun CornerSize(percent: Int): CornerSize = CornerSize(percent.toFloat(), CornerSize.Type.Percent)

object ShapeDefaults {

  internal val CornerFull: CornerSize =CornerSize(100)
  internal val Small: RoundedCornerShape = RoundedCornerShape(8.0.dp)
}
