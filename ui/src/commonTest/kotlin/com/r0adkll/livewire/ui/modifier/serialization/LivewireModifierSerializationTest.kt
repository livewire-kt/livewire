package com.r0adkll.livewire.ui.modifier.serialization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.alpha
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.border
import com.r0adkll.livewire.ui.modifier.clickable
import com.r0adkll.livewire.ui.modifier.clip
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.width
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.Json
import okio.utf8Size
import kotlin.test.Test
import kotlin.test.assertTrue

class LivewireModifierSerializationTest {

  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun cborIsSmallerThanJsonSerialization() {
    val cbor = Cbor {
      serializersModule = LivewireModifierSerializers().serializersModule
    }

    val json = Json {
      serializersModule = LivewireModifierSerializers().serializersModule
    }

    val modifier = LivewireModifier
      .width(20.dp)
      .height(60.dp)
      .padding(16.dp)
      .background(Color.Cyan)
      .border(2.dp, Color.Blue)
      .alpha(0.5f)
      .clickable(ClickAction("test"))
      .clip(CircleShape)

    val cborResult = cbor.encodeToHexString(modifier)
    val jsonResult = json.encodeToString(cborResult)

    println("cbor size: ${cborResult.length}")
    println("json size: ${jsonResult.length}")

    assertTrue {
      cborResult.length < jsonResult.length
    }
  }
}
