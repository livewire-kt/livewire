package com.livewire.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.ui.graphics.CornerSize
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.graphics.Shape
import com.livewire.ui.graphics.ShapeDefaults

object ButtonGroupDefaults {

  val ConnectedSpaceBetween: Dp = 2.dp

  val InnerCornerCornerSize: Dp = 8.dp
  val PressedInnerCornerCornerSize: Dp = 4.dp

  val connectedLeadingButtonShape: Shape
    @Composable
    get() =
      RoundedCornerShape(
        topStart = ShapeDefaults.CornerFull,
        bottomStart = ShapeDefaults.CornerFull,
        topEnd = CornerSize(InnerCornerCornerSize),
        bottomEnd = CornerSize(InnerCornerCornerSize),
      )

  val connectedLeadingButtonPressShape: Shape
    @Composable
    get() = RoundedCornerShape(
      topStart = ShapeDefaults.CornerFull,
      bottomStart = ShapeDefaults.CornerFull,
      topEnd = CornerSize(PressedInnerCornerCornerSize),
      bottomEnd = CornerSize(PressedInnerCornerCornerSize),
    )

  val connectedTrailingButtonShape: Shape
    @Composable
    get() = RoundedCornerShape(
      topEnd = ShapeDefaults.CornerFull,
      bottomEnd = ShapeDefaults.CornerFull,
      topStart = CornerSize(InnerCornerCornerSize),
      bottomStart = CornerSize(InnerCornerCornerSize),
    )

  val connectedTrailingButtonPressShape: Shape
    @Composable
    get() = RoundedCornerShape(
      topEnd = ShapeDefaults.CornerFull,
      bottomEnd = ShapeDefaults.CornerFull,
      topStart = CornerSize(PressedInnerCornerCornerSize),
      bottomStart = CornerSize(PressedInnerCornerCornerSize),
    )

  val connectedButtonCheckedShape = RoundedCornerShape(ShapeDefaults.CornerFull)

  val connectedMiddleButtonPressShape: Shape
    @Composable
    get() = RoundedCornerShape(PressedInnerCornerCornerSize)

  @Composable
  fun connectedLeadingButtonShapes(
    shape: Shape = connectedLeadingButtonShape,
    pressedShape: Shape = connectedLeadingButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
  ): ToggleButtonShapes = ToggleButtonShapes(
    shape = shape,
    pressedShape = pressedShape,
    checkedShape = checkedShape
  )

  @Composable
  fun connectedMiddleButtonShapes(
    shape: Shape = ShapeDefaults.Small,
    pressedShape: Shape = connectedMiddleButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
  ): ToggleButtonShapes =
    ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)

  @Composable
  fun connectedTrailingButtonShapes(
    shape: Shape = connectedTrailingButtonShape,
    pressedShape: Shape = connectedTrailingButtonPressShape,
    checkedShape: Shape = connectedButtonCheckedShape,
  ): ToggleButtonShapes =
    ToggleButtonShapes(shape = shape, pressedShape = pressedShape, checkedShape = checkedShape)
}
