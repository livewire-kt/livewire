package com.livewire.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.livewire.ui.actions.ValueChangeAction
import com.livewire.ui.data.LivewireUiJson
import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.text.LivewireFontFamily
import com.livewire.ui.text.LivewireTextStyle
import com.livewire.ui.text.TypographyToken
import com.livewire.ui.widget.BasicTextFieldNode
import com.livewire.ui.widget.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class LivewireTextStyleSerializationTest {

  private val fullStyle = LivewireTextStyle(
    token = TypographyToken.TitleMedium,
    color = Color.Red,
    fontSize = 18f.sp,
    fontWeight = FontWeight.SemiBold,
    fontStyle = FontStyle.Italic,
    fontFamily = LivewireFontFamily.Monospace,
    letterSpacing = 0.5f.em,
    textDecoration = TextDecoration.Underline + TextDecoration.LineThrough,
    textAlign = TextAlign.Center,
    lineHeight = 24f.sp,
    background = Color.Yellow,
    shadow = Shadow(Color.Black, Offset(1f, 2f), blurRadius = 3f),
  )

  @Test
  fun textNodeWithFullStyleRoundTripsThroughProtobuf() {
    val node = TextNode(text = "Hello", style = fullStyle)

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<TextNode>(bytes)

    assertEquals(node.text, decoded.text)
    assertEquals(fullStyle, decoded.style)
  }

  @Test
  fun textNodeWithFullStyleRoundTripsThroughJson() {
    val node = TextNode(text = "Hello", style = fullStyle)

    val json = LivewireUiJson.encodeToString(node)
    val decoded = LivewireUiJson.decodeFromString(TextNode.serializer(), json)

    assertEquals(fullStyle, decoded.style)
  }

  @Test
  fun tokenOnlyStyleRoundTripsThroughProtobuf() {
    val style = LivewireTextStyle(token = TypographyToken.BodyLarge)
    val node = TextNode(text = "Hello", style = style)

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<TextNode>(bytes)

    assertEquals(style, decoded.style)
    assertEquals(TextUnit.Unspecified, decoded.style!!.fontSize)
    assertEquals(Color.Unspecified, decoded.style!!.color)
  }

  @Test
  fun fieldsOnlyStyleRoundTripsThroughProtobuf() {
    val style = LivewireTextStyle(
      fontSize = 22f.sp,
      fontWeight = FontWeight.Bold,
    )
    val node = TextNode(text = "Hello", style = style)

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<TextNode>(bytes)

    assertEquals(style, decoded.style)
    assertNull(decoded.style!!.token)
  }

  @Test
  fun nullStyleRoundTripsThroughProtobuf() {
    val node = TextNode(text = "Hello")

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<TextNode>(bytes)

    assertNull(decoded.style)
  }

  @Test
  fun basicTextFieldNodeStyleRoundTripsThroughProtobuf() {
    val node = BasicTextFieldNode(
      initialValue = "value",
      onValueChange = ValueChangeAction("onValueChange_1"),
      textStyle = fullStyle,
    )

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<BasicTextFieldNode>(bytes)

    assertEquals(fullStyle, decoded.textStyle)
  }
}
