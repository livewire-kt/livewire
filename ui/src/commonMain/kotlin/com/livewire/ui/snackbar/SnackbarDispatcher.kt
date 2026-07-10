package com.livewire.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SnackbarDispatcher internal constructor(
  private val host: SnackbarHostState,
  private val scope: CoroutineScope,
) {

  private var currentSnackJob: Job? = null

  fun showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    onAction: () -> Unit = {},
  ) {
    currentSnackJob?.cancel()
    currentSnackJob = scope.launch {
      if (host.showSnackbar(message, actionLabel, withDismissAction, duration) == SnackbarResult.ActionPerformed) {
        onAction()
      }
    }
  }
}

val LocalSnackDispatcher = staticCompositionLocalOf<SnackbarDispatcher> {
  error("Snackbar dispatching is not setup in this composition")
}

@Composable
fun rememberSnackbarDispatcher(
  snackbarHostState: SnackbarHostState,
): SnackbarDispatcher {
  val scope = rememberCoroutineScope()
  return remember(snackbarHostState) {
    SnackbarDispatcher(
      host = snackbarHostState,
      scope = scope,
    )
  }
}
