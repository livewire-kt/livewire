package com.r0adkll.livewire.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

internal val DrawerWidth = 256.dp

@Composable
fun HostScaffold(
  topBar: @Composable () -> Unit,
  drawer: @Composable ColumnScope.() -> Unit,
  snackbarHost: @Composable BoxScope.() -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = modifier
      .background(MaterialTheme.colorScheme.background)
      .fillMaxSize()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .zIndex(1f),
      ) {
        topBar()
      }
      PermanentNavigationDrawer(
        drawerContent = {
          drawer()
        },
      ) {
        content()
      }
    }

    snackbarHost()
  }
}

@Composable
internal fun HostDrawerSheet(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier.widthIn(max = DrawerWidth),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shadowElevation = 2.dp
  ) {
    content()
  }
}
