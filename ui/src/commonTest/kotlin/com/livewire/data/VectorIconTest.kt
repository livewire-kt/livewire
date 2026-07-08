package com.livewire.data

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.unit.dp
import com.livewire.protocol.EnvelopeJson
import com.livewire.ui.PluginInfo
import com.livewire.ui.data.LivewireUiJson
import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.data.PluginSelected
import com.livewire.ui.data.UiProtocol
import com.livewire.ui.graphics.VectorIcon
import com.livewire.ui.graphics.toVectorIcon
import com.livewire.ui.widget.IconNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class VectorIconTest {

  @Test
  fun imageVectorRoundTripsThroughVectorIcon() {
    val original = sampleIcon()
    val roundTripped = original.toVectorIcon().toImageVector()
    assertEquals(original, roundTripped)
  }

  @Test
  fun minimalImageVectorRoundTripsThroughVectorIcon() {
    val original = minimalIcon()
    val roundTripped = original.toVectorIcon().toImageVector()
    assertEquals(original, roundTripped)
  }

  @Test
  fun vectorIconRoundTripsThroughProtobuf() {
    val vectorIcon = sampleIcon().toVectorIcon()

    val bytes = LivewireUiProtobuf.encodeToByteArray(vectorIcon)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<VectorIcon>(bytes)

    assertEquals(vectorIcon, decoded)
    assertEquals(sampleIcon(), decoded.toImageVector())
  }

  @Test
  fun minimalVectorIconRoundTripsThroughProtobuf() {
    val vectorIcon = minimalIcon().toVectorIcon()

    val bytes = LivewireUiProtobuf.encodeToByteArray(vectorIcon)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<VectorIcon>(bytes)

    assertEquals(vectorIcon, decoded)
  }

  @Test
  fun vectorIconRoundTripsThroughJson() {
    val vectorIcon = sampleIcon().toVectorIcon()

    val json = LivewireUiJson.encodeToString(vectorIcon)
    val decoded = LivewireUiJson.decodeFromString<VectorIcon>(json)

    assertEquals(vectorIcon, decoded)
  }

  @Test
  fun iconNodeWithVectorRoundTripsThroughProtobuf() {
    val node = IconNode().apply {
      vector = sampleIcon().toVectorIcon()
      tint = Color.Red
    }

    val bytes = LivewireUiProtobuf.encodeToByteArray(node)
    val decoded = LivewireUiProtobuf.decodeFromByteArray<IconNode>(bytes)

    assertEquals(node.vector, decoded.vector)
    assertEquals(node.tint, decoded.tint)
  }

  @Test
  fun pluginInfoWithVectorIconRoundTripsThroughEnvelopeJson() {
    val payload: UiProtocol = PluginSelected(
      PluginInfo(
        pluginId = "network",
        title = "Network",
        icon = sampleIcon().toVectorIcon(),
      ),
    )

    val json = EnvelopeJson.encodeToString(UiProtocol.serializer(), payload)
    val decoded = EnvelopeJson.decodeFromString(UiProtocol.serializer(), json)

    assertEquals(payload, decoded)
  }

  @Test
  fun gradientBrushesAreRejected() {
    val icon = ImageVector.Builder(
      name = "Gradient",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      addPath(
        pathData = listOf(
          PathNode.MoveTo(0f, 0f),
          PathNode.LineTo(24f, 24f),
          PathNode.Close,
        ),
        fill = Brush.linearGradient(listOf(Color.Red, Color.Blue)),
      )
    }.build()

    assertFailsWith<IllegalArgumentException> { icon.toVectorIcon() }
  }

  /**
   * Exercises groups (transforms + clip), fills, strokes, every stroke cap/join
   * variant we map, fill types, trim paths, and every path command type.
   */
  private fun sampleIcon(): ImageVector = ImageVector.Builder(
    name = "Sample",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    tintColor = Color.Blue,
    autoMirror = true,
  ).apply {
    addPath(
      pathData = listOf(
        PathNode.MoveTo(12f, 2f),
        PathNode.LineTo(19f, 21f),
        PathNode.RelativeLineTo(-14f, 0f),
        PathNode.HorizontalTo(5f),
        PathNode.RelativeHorizontalTo(1f),
        PathNode.VerticalTo(20f),
        PathNode.RelativeVerticalTo(-1f),
        PathNode.CurveTo(6f, 18f, 7f, 17f, 8f, 16f),
        PathNode.RelativeCurveTo(1f, -1f, 2f, -1f, 3f, 0f),
        PathNode.ReflectiveCurveTo(12f, 14f, 13f, 13f),
        PathNode.RelativeReflectiveCurveTo(1f, -1f, 2f, 0f),
        PathNode.Close,
      ),
      fill = SolidColor(Color.Black),
      fillAlpha = 0.8f,
      pathFillType = PathFillType.EvenOdd,
    )
    addGroup(
      name = "badge",
      rotate = 45f,
      pivotX = 12f,
      pivotY = 12f,
      scaleX = 1.5f,
      scaleY = 0.5f,
      translationX = 2f,
      translationY = -2f,
      clipPathData = listOf(
        PathNode.MoveTo(0f, 0f),
        PathNode.LineTo(24f, 0f),
        PathNode.LineTo(24f, 24f),
        PathNode.LineTo(0f, 24f),
        PathNode.Close,
      ),
    )
    addPath(
      pathData = listOf(
        PathNode.RelativeMoveTo(4f, 4f),
        PathNode.QuadTo(8f, 2f, 12f, 4f),
        PathNode.RelativeQuadTo(2f, 1f, 4f, 0f),
        PathNode.ReflectiveQuadTo(20f, 8f),
        PathNode.RelativeReflectiveQuadTo(1f, 2f),
        PathNode.ArcTo(5f, 5f, 30f, false, true, 12f, 20f),
        PathNode.RelativeArcTo(3f, 3f, 0f, true, false, -6f, 0f),
        PathNode.Close,
      ),
      name = "badgeStroke",
      stroke = SolidColor(Color.Red),
      strokeAlpha = 0.9f,
      strokeLineWidth = 2f,
      strokeLineCap = StrokeCap.Round,
      strokeLineJoin = StrokeJoin.Bevel,
      strokeLineMiter = 6f,
      trimPathStart = 0.1f,
      trimPathEnd = 0.9f,
      trimPathOffset = 0.25f,
    )
    clearGroup()
  }.build()

  private fun minimalIcon(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    addPath(
      pathData = listOf(
        PathNode.MoveTo(2f, 2f),
        PathNode.LineTo(22f, 22f),
        PathNode.Close,
      ),
      fill = SolidColor(Color.Black),
    )
  }.build()
}
