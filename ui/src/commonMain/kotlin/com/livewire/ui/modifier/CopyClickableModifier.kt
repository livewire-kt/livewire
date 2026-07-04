package com.livewire.ui.modifier

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.host.snackbar.LocalSnackDispatcher
import kotlinx.serialization.Serializable

@LivewireSerializer
@Serializable
internal class CopyClickableModifier(
  val value: String,
  val enabled: Boolean = true,
) : LivewireModifier.Element {

  @Suppress("ModifierFactoryExtensionFunction")
  @Composable
  override fun toComposeUi(then: Modifier): Modifier {
    val clipboardManager = LocalClipboardManager.current
    val snackbarDispatcher = LocalSnackDispatcher.current
    return then.clickable(enabled = enabled) {
      clipboardManager.setText(AnnotatedString(value))
      snackbarDispatcher.showSnackbar(
        message = "\"${value.take(50)}${if (value.length > 50) "…" else ""}\" copied to the clipboard",
      )
    }
  }
}

fun LivewireModifier.copyClickable(value: String): LivewireModifier =
  then(CopyClickableModifier(value))

fun LivewireModifier.copyClickable(value: String, enabled: Boolean): LivewireModifier =
  then(CopyClickableModifier(value, enabled))
