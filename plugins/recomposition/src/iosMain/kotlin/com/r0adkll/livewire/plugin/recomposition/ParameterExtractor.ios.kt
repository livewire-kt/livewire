@file:OptIn(ComposeToolingApi::class)

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.ParameterSourceInformation

@ComposeToolingApi
internal actual fun extractParametersFromLambda(
  scope: RecomposeScope,
  metadata: List<ParameterSourceInformation>,
): List<ParameterInfo>? = null
