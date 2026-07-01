package com.r0adkll.livewire.ui.widget

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableComposeNode
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.toLong
import com.r0adkll.livewire.annotations.LivewireSerializer
import com.r0adkll.livewire.ui.composition.LivewireComposable
import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.applier
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import kotlinx.serialization.Serializable

@LivewireComposable
@Composable
fun Image(
  imageData: ByteArray,
  contentDescription: String? = null,
  modifier: LivewireModifier = LivewireModifier,
) {
  val compositeKeyHash = currentCompositeKeyHashCode.toLong()
  ReusableComposeNode<ImageNode, Applier<LayoutNode>>(
    factory = { ImageNode(imageData) },
    update = {
      set(modifier, LayoutNode.SetModifier)
      init(compositeKeyHash, LayoutNode.SetCompositeKeyHash)
      set(imageData, ImageNode.SetImageData)
      set(contentDescription, ImageNode.SetContentDescription)
    }
  )
}

@LivewireSerializer
@Serializable
class ImageNode(
  var imageData: ByteArray,
) : LayoutNode() {
  var contentDescription: String? = null

  companion object {
    val SetImageData: ImageNode.(ByteArray) -> Unit = applier { imageData = it }
    val SetContentDescription: ImageNode.(String?) -> Unit = applier { contentDescription = it }
  }
}
