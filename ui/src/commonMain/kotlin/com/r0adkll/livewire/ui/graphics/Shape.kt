package com.r0adkll.livewire.ui.graphics

import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape as ComposeCircleShape
import androidx.compose.foundation.shape.RoundedCornerShape as ComposeRoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape as ComposeRectangleShape
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import androidx.compose.foundation.shape.CornerSize as ComposeCornerSize
import androidx.compose.ui.graphics.Shape as ComposeShape

@Serializable
sealed interface Shape {

  fun toComposeUi(): ComposeShape {
    return when (this) {
      CircleShape -> ComposeCircleShape
      is RectangleShape -> ComposeRectangleShape
      is RoundedCornerShape -> ComposeRoundedCornerShape(
        cornerSize.toComposeUi()
      )
    }
  }
}

@Serializable
data object RectangleShape : Shape

@Serializable
data object CircleShape : Shape

@Serializable
data class RoundedCornerShape(
  val cornerSize: CornerSize
) : Shape

fun RoundedCornerShape(size: Dp) = RoundedCornerShape(CornerSize(size))
fun RoundedCornerShape(pixels: Float) = RoundedCornerShape(CornerSize(pixels))
fun RoundedCornerShape(percentage: Int) = RoundedCornerShape(CornerSize(percentage))

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
