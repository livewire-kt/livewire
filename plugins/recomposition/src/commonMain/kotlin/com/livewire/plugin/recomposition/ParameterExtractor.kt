package com.livewire.plugin.recomposition

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.ParameterSourceInformation

@ComposeToolingApi
internal expect fun extractParametersFromLambda(
  scope: RecomposeScope,
  metadata: List<ParameterSourceInformation>,
): List<ParameterInfo>?
