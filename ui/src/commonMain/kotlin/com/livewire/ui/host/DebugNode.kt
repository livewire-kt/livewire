package com.livewire.ui.host

import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


/**
 * Set this to true to draw debugging information on the screen
 */
var DebugNodes by mutableStateOf(false)

fun Modifier.debugFrame(): Modifier = if (DebugNodes) {
  border(
    width = 1.dp,
    color = Color.Red
  )
} else this
