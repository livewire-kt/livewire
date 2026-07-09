package com.livewire.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import livewire.host.generated.resources.Res
import livewire.host.generated.resources.blackhansans
import org.jetbrains.compose.resources.Font

val BlackHanSans: FontFamily
  @Composable get() = FontFamily(
    Font(Res.font.blackhansans)
  )
