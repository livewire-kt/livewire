
package com.r0adkll.livewire.ui.layout

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.actions.LocalLivewireActionDispatcher
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.CheckboxNode
import com.r0adkll.livewire.ui.widget.TextNode
import com.r0adkll.livewire.ui.widget.TextStyle
import kotlinx.coroutines.launch
import com.r0adkll.livewire.ui.layout.Alignment as LivewireAlignment

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CheckboxNodeContent(
  node: CheckboxNode,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val eventDispatcher = LocalLivewireActionDispatcher.current

  Checkbox(
    checked = node.checked,
    onCheckedChange = {
      scope.launch {
        eventDispatcher.dispatch(node.onCheckedChange.copy(checked = it))
      }
    },
    modifier = modifier.debugFrame(),
    enabled = node.enabled,
  )
}
