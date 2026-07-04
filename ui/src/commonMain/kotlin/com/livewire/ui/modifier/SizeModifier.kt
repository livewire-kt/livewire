package com.livewire.ui.modifier

import androidx.compose.ui.unit.Dp
import com.livewire.ui.modifier.DimensionModifier.Type

fun LivewireModifier.size(width: Dp, height: Dp): LivewireModifier =
  width(width).height(height)

fun LivewireModifier.size(size: Dp): LivewireModifier = width(size).height(size)

fun LivewireModifier.fillMaxSize(fraction: Float = 1f): LivewireModifier =
  fillMaxWidth(fraction).fillMaxHeight(fraction)

fun LivewireModifier.wrapContentSize(): LivewireModifier =
  then(WidthModifier(Type.WRAP, 1f)).then(HeightModifier(Type.WRAP, 1f))
