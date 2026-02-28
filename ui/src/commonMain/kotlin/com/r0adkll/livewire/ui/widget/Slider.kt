package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.actions.FloatValueChangeAction
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Slider(
  value: Float,
  onValueChange: FloatValueChangeAction,
  modifier: LivewireModifier = LivewireModifier,
  enabled: Boolean = true,
  valueRangeStart: Float = 0f,
  valueRangeEnd: Float = 1f,
  steps: Int = 0,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.hashCode()
  ReusableComposeNode<SliderNode, Applier<LayoutNode>>(
    factory = { SliderNode(value, onValueChange, enabled, valueRangeStart, valueRangeEnd, steps) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(value, SliderNode.SetValue)
      set(onValueChange, SliderNode.SetOnValueChange)
      set(enabled, SliderNode.SetEnabled)
      set(valueRangeStart, SliderNode.SetValueRangeStart)
      set(valueRangeEnd, SliderNode.SetValueRangeEnd)
      set(steps, SliderNode.SetSteps)
    }
  )
}

@LivewireSerializer
@Serializable
class SliderNode(
  var value: Float,
  var onValueChange: FloatValueChangeAction,
  var enabled: Boolean,
  var valueRangeStart: Float,
  var valueRangeEnd: Float,
  var steps: Int,
) : LayoutNode() {

  companion object {
    val SetValue: SliderNode.(Float) -> Unit = applier { value = it }
    val SetOnValueChange: SliderNode.(FloatValueChangeAction) -> Unit = applier { onValueChange = it }
    val SetEnabled: SliderNode.(Boolean) -> Unit = applier { enabled = it }
    val SetValueRangeStart: SliderNode.(Float) -> Unit = applier { valueRangeStart = it }
    val SetValueRangeEnd: SliderNode.(Float) -> Unit = applier { valueRangeEnd = it }
    val SetSteps: SliderNode.(Int) -> Unit = applier { steps = it }
  }
}
