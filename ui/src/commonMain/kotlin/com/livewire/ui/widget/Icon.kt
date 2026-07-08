package com.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.remember
import androidx.compose.runtime.toLong
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.livewire.annotations.LivewireSerializer
import com.livewire.ui.composition.LivewireComposable
import com.livewire.ui.graphics.ColorSerializer
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.graphics.toVectorIcon
import com.livewire.ui.layout.LayoutNode
import com.livewire.ui.layout.applier
import com.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Icon(
  imageVector: ImageVector,
  modifier: LivewireModifier = LivewireModifier,
  tint: Color = Color.Unspecified,
) {
  val vector = remember(imageVector) { imageVector.toVectorIcon() }
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<IconNode, Applier<LayoutNode>>(
    factory = { IconNode() },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(vector, IconNode.SetVector)
      set(tint, IconNode.SetTint)
    },
  )
}

@LivewireSerializer
@Serializable
class IconNode(
  var vector: VectorIcon? = null,
) : LayoutNode() {
  @Serializable(with = ColorSerializer::class)
  var tint: Color = Color.Unspecified

  companion object {
    val SetVector: IconNode.(VectorIcon?) -> Unit = applier { vector = it }
    val SetTint: IconNode.(Color) -> Unit = applier { tint = it }
  }
}
