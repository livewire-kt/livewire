package com.livewire.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

val LocalDarkMode = compositionLocalOf { false }

@Composable
fun currentDarkMode() = LocalDarkMode.current
