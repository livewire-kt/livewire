package com.r0adkll.livewire.ui.data

import com.r0adkll.livewire.ui.layout.LayoutNode
import com.r0adkll.livewire.ui.layout.RootNode
import com.r0adkll.livewire.ui.layout.BoxNode
import com.r0adkll.livewire.ui.layout.ColumnNode
import com.r0adkll.livewire.ui.layout.RowNode
import com.r0adkll.livewire.ui.widget.ButtonNode
import com.r0adkll.livewire.ui.widget.TextNode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val LivewireUiJson = Json {
  classDiscriminator = "type"
  serializersModule = SerializersModule {
    polymorphic(LayoutNode::class) {
      subclass(RootNode::class, RootNode.serializer())
      subclass(BoxNode::class, BoxNode.serializer())
      subclass(ColumnNode::class, ColumnNode.serializer())
      subclass(RowNode::class, RowNode.serializer())
      subclass(TextNode::class, TextNode.serializer())
      subclass(ButtonNode::class, ButtonNode.serializer())
    }
  }
}
