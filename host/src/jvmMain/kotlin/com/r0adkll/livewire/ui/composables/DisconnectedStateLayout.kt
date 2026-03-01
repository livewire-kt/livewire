package com.r0adkll.livewire.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import livewire.host.generated.resources.Res
import livewire.host.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DisconnectedStateLayout(
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {

    Image(
      painterResource(Res.drawable.logo),
      contentDescription = null,
      modifier = Modifier.size(256.dp)
    )

    Text(
      text = "Connect to a Livewire application",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.SemiBold,
    )

  }
}
