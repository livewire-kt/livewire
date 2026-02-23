package com.r0adkll.livewire.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private val DrawerWidth = 300.dp

@Composable
fun HostScaffold(
  topBar: @Composable () -> Unit,
  drawer: @Composable ColumnScope.() -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier.fillMaxSize()) {
    Box(
      modifier = Modifier.fillMaxWidth()
        .zIndex(1f),
    ) {
      topBar()
    }
    PermanentNavigationDrawer(
      drawerContent = {
        PermanentDrawerSheet(
          drawerShape = RoundedCornerShape(
            bottomEnd = 16.dp,
          ),
          modifier = Modifier.width(DrawerWidth),
        ) {
          drawer()
        }
      },
    ) {
      content()
    }
  }
}
