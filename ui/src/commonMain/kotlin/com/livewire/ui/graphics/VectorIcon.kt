package com.livewire.ui.graphics

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.dp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A serializable mirror of [ImageVector] so vector icons can be sent over the wire as
 * structured data and re-materialized as a true [ImageVector] on the host, where they
 * render through a vector painter at full quality (instead of being rasterized from SVG).
 *
 * Current limitations:
 * - Only solid color fills and strokes are supported (see [IconBrush]). Gradient brush
 *   parameters are not readable through Compose's public API.
 * - `tintBlendMode` is not carried over; the host always uses the default (SrcIn).
 */
@Immutable
@Serializable
data class VectorIcon(
  val name: String = "",
  val defaultWidthDp: Float,
  val defaultHeightDp: Float,
  val viewportWidth: Float,
  val viewportHeight: Float,
  @Serializable(with = ColorSerializer::class)
  val tintColor: Color = Color.Unspecified,
  val autoMirror: Boolean = false,
  val root: Group,
) {

  @Immutable
  @Serializable
  sealed interface Node

  @Immutable
  @Serializable
  @SerialName("g")
  data class Group(
    val name: String = "",
    val rotation: Float = 0f,
    val pivotX: Float = 0f,
    val pivotY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val clipPathData: List<PathCommand> = emptyList(),
    val children: List<Node> = emptyList(),
  ) : Node {
    internal val isIdentity: Boolean
      get() = name.isEmpty() &&
        rotation == 0f && pivotX == 0f && pivotY == 0f &&
        scaleX == 1f && scaleY == 1f &&
        translationX == 0f && translationY == 0f &&
        clipPathData.isEmpty()
  }

  @Immutable
  @Serializable
  @SerialName("p")
  data class Path(
    val name: String = "",
    val pathData: List<PathCommand>,
    val fillType: FillType = FillType.NonZero,
    val fill: IconBrush? = null,
    val fillAlpha: Float = 1f,
    val stroke: IconBrush? = null,
    val strokeAlpha: Float = 1f,
    val strokeLineWidth: Float = 0f,
    val strokeLineCap: Cap = Cap.Butt,
    val strokeLineJoin: Join = Join.Miter,
    val strokeLineMiter: Float = 4f,
    val trimPathStart: Float = 0f,
    val trimPathEnd: Float = 1f,
    val trimPathOffset: Float = 0f,
  ) : Node

  @Serializable
  enum class FillType { NonZero, EvenOdd }

  @Serializable
  enum class Cap { Butt, Round, Square }

  @Serializable
  enum class Join { Miter, Round, Bevel }

  fun toImageVector(): ImageVector {
    val builder = ImageVector.Builder(
      name = name,
      defaultWidth = defaultWidthDp.dp,
      defaultHeight = defaultHeightDp.dp,
      viewportWidth = viewportWidth,
      viewportHeight = viewportHeight,
      tintColor = tintColor,
      autoMirror = autoMirror,
    )
    // The builder already starts inside an implicit identity root group, so only
    // re-wrap the root when it carries a transform or clip of its own.
    if (root.isIdentity) {
      root.children.forEach { builder.add(it) }
    } else {
      builder.add(root)
    }
    return builder.build()
  }
}

/**
 * Serializable brush for [VectorIcon] fills and strokes. Solid colors only for now;
 * gradients require either a Livewire-side brush DSL or reflection to extract from
 * Compose's gradient brushes, whose parameters are internal.
 */
@Immutable
@Serializable
sealed interface IconBrush {
  fun toComposeUi(): Brush

  @Immutable
  @Serializable
  @SerialName("solid")
  data class Solid(
    @Serializable(with = ColorSerializer::class)
    val color: Color,
  ) : IconBrush {
    override fun toComposeUi(): Brush = SolidColor(color)
  }
}

/**
 * Serializable mirror of [PathNode]. Serial names match the equivalent SVG path
 * command letters to keep the wire encoding compact.
 */
@Immutable
@Serializable
sealed interface PathCommand {
  @Serializable
  @SerialName("Z")
  data object Close : PathCommand

  @Serializable
  @SerialName("M")
  data class MoveTo(val x: Float, val y: Float) : PathCommand

  @Serializable
  @SerialName("m")
  data class RelativeMoveTo(val dx: Float, val dy: Float) : PathCommand

  @Serializable
  @SerialName("L")
  data class LineTo(val x: Float, val y: Float) : PathCommand

  @Serializable
  @SerialName("l")
  data class RelativeLineTo(val dx: Float, val dy: Float) : PathCommand

  @Serializable
  @SerialName("H")
  data class HorizontalTo(val x: Float) : PathCommand

  @Serializable
  @SerialName("h")
  data class RelativeHorizontalTo(val dx: Float) : PathCommand

  @Serializable
  @SerialName("V")
  data class VerticalTo(val y: Float) : PathCommand

  @Serializable
  @SerialName("v")
  data class RelativeVerticalTo(val dy: Float) : PathCommand

  @Serializable
  @SerialName("C")
  data class CurveTo(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val x3: Float,
    val y3: Float,
  ) : PathCommand

  @Serializable
  @SerialName("c")
  data class RelativeCurveTo(
    val dx1: Float,
    val dy1: Float,
    val dx2: Float,
    val dy2: Float,
    val dx3: Float,
    val dy3: Float,
  ) : PathCommand

  @Serializable
  @SerialName("S")
  data class ReflectiveCurveTo(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
  ) : PathCommand

  @Serializable
  @SerialName("s")
  data class RelativeReflectiveCurveTo(
    val dx1: Float,
    val dy1: Float,
    val dx2: Float,
    val dy2: Float,
  ) : PathCommand

  @Serializable
  @SerialName("Q")
  data class QuadTo(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
  ) : PathCommand

  @Serializable
  @SerialName("q")
  data class RelativeQuadTo(
    val dx1: Float,
    val dy1: Float,
    val dx2: Float,
    val dy2: Float,
  ) : PathCommand

  @Serializable
  @SerialName("T")
  data class ReflectiveQuadTo(val x: Float, val y: Float) : PathCommand

  @Serializable
  @SerialName("t")
  data class RelativeReflectiveQuadTo(val dx: Float, val dy: Float) : PathCommand

  @Serializable
  @SerialName("A")
  data class ArcTo(
    val horizontalEllipseRadius: Float,
    val verticalEllipseRadius: Float,
    val theta: Float,
    val isMoreThanHalf: Boolean,
    val isPositiveArc: Boolean,
    val arcStartX: Float,
    val arcStartY: Float,
  ) : PathCommand

  @Serializable
  @SerialName("a")
  data class RelativeArcTo(
    val horizontalEllipseRadius: Float,
    val verticalEllipseRadius: Float,
    val theta: Float,
    val isMoreThanHalf: Boolean,
    val isPositiveArc: Boolean,
    val arcStartDx: Float,
    val arcStartDy: Float,
  ) : PathCommand
}

fun ImageVector.toVectorIcon(): VectorIcon = VectorIcon(
  name = name,
  defaultWidthDp = defaultWidth.value,
  defaultHeightDp = defaultHeight.value,
  viewportWidth = viewportWidth,
  viewportHeight = viewportHeight,
  tintColor = tintColor,
  autoMirror = autoMirror,
  root = root.toVectorIconGroup(),
)

private fun VectorGroup.toVectorIconGroup(): VectorIcon.Group = VectorIcon.Group(
  name = name,
  rotation = rotation,
  pivotX = pivotX,
  pivotY = pivotY,
  scaleX = scaleX,
  scaleY = scaleY,
  translationX = translationX,
  translationY = translationY,
  clipPathData = clipPathData.map { it.toPathCommand() },
  children = map { child ->
    when (child) {
      is VectorGroup -> child.toVectorIconGroup()
      is VectorPath -> child.toVectorIconPath()
    }
  },
)

private fun VectorPath.toVectorIconPath(): VectorIcon.Path = VectorIcon.Path(
  name = name,
  pathData = pathData.map { it.toPathCommand() },
  fillType = when (pathFillType) {
    PathFillType.EvenOdd -> VectorIcon.FillType.EvenOdd
    else -> VectorIcon.FillType.NonZero
  },
  fill = fill?.toIconBrush(),
  fillAlpha = fillAlpha,
  stroke = stroke?.toIconBrush(),
  strokeAlpha = strokeAlpha,
  strokeLineWidth = strokeLineWidth,
  strokeLineCap = when (strokeLineCap) {
    StrokeCap.Round -> VectorIcon.Cap.Round
    StrokeCap.Square -> VectorIcon.Cap.Square
    else -> VectorIcon.Cap.Butt
  },
  strokeLineJoin = when (strokeLineJoin) {
    StrokeJoin.Round -> VectorIcon.Join.Round
    StrokeJoin.Bevel -> VectorIcon.Join.Bevel
    else -> VectorIcon.Join.Miter
  },
  strokeLineMiter = strokeLineMiter,
  trimPathStart = trimPathStart,
  trimPathEnd = trimPathEnd,
  trimPathOffset = trimPathOffset,
)

private fun Brush.toIconBrush(): IconBrush = when (this) {
  is SolidColor -> IconBrush.Solid(value)
  else -> throw IllegalArgumentException(
    "Unsupported brush ${this::class.simpleName}: VectorIcon currently supports " +
      "solid color fills and strokes only."
  )
}

private fun ImageVector.Builder.add(node: VectorIcon.Node) {
  when (node) {
    is VectorIcon.Group -> {
      addGroup(
        name = node.name,
        rotate = node.rotation,
        pivotX = node.pivotX,
        pivotY = node.pivotY,
        scaleX = node.scaleX,
        scaleY = node.scaleY,
        translationX = node.translationX,
        translationY = node.translationY,
        clipPathData = node.clipPathData.map { it.toPathNode() },
      )
      node.children.forEach { add(it) }
      clearGroup()
    }
    is VectorIcon.Path -> addPath(
      pathData = node.pathData.map { it.toPathNode() },
      pathFillType = when (node.fillType) {
        VectorIcon.FillType.NonZero -> PathFillType.NonZero
        VectorIcon.FillType.EvenOdd -> PathFillType.EvenOdd
      },
      name = node.name,
      fill = node.fill?.toComposeUi(),
      fillAlpha = node.fillAlpha,
      stroke = node.stroke?.toComposeUi(),
      strokeAlpha = node.strokeAlpha,
      strokeLineWidth = node.strokeLineWidth,
      strokeLineCap = when (node.strokeLineCap) {
        VectorIcon.Cap.Butt -> StrokeCap.Butt
        VectorIcon.Cap.Round -> StrokeCap.Round
        VectorIcon.Cap.Square -> StrokeCap.Square
      },
      strokeLineJoin = when (node.strokeLineJoin) {
        VectorIcon.Join.Miter -> StrokeJoin.Miter
        VectorIcon.Join.Round -> StrokeJoin.Round
        VectorIcon.Join.Bevel -> StrokeJoin.Bevel
      },
      strokeLineMiter = node.strokeLineMiter,
      trimPathStart = node.trimPathStart,
      trimPathEnd = node.trimPathEnd,
      trimPathOffset = node.trimPathOffset,
    )
  }
}

private fun PathNode.toPathCommand(): PathCommand = when (this) {
  is PathNode.Close -> PathCommand.Close
  is PathNode.MoveTo -> PathCommand.MoveTo(x, y)
  is PathNode.RelativeMoveTo -> PathCommand.RelativeMoveTo(dx, dy)
  is PathNode.LineTo -> PathCommand.LineTo(x, y)
  is PathNode.RelativeLineTo -> PathCommand.RelativeLineTo(dx, dy)
  is PathNode.HorizontalTo -> PathCommand.HorizontalTo(x)
  is PathNode.RelativeHorizontalTo -> PathCommand.RelativeHorizontalTo(dx)
  is PathNode.VerticalTo -> PathCommand.VerticalTo(y)
  is PathNode.RelativeVerticalTo -> PathCommand.RelativeVerticalTo(dy)
  is PathNode.CurveTo -> PathCommand.CurveTo(x1, y1, x2, y2, x3, y3)
  is PathNode.RelativeCurveTo -> PathCommand.RelativeCurveTo(dx1, dy1, dx2, dy2, dx3, dy3)
  is PathNode.ReflectiveCurveTo -> PathCommand.ReflectiveCurveTo(x1, y1, x2, y2)
  is PathNode.RelativeReflectiveCurveTo ->
    PathCommand.RelativeReflectiveCurveTo(dx1, dy1, dx2, dy2)
  is PathNode.QuadTo -> PathCommand.QuadTo(x1, y1, x2, y2)
  is PathNode.RelativeQuadTo -> PathCommand.RelativeQuadTo(dx1, dy1, dx2, dy2)
  is PathNode.ReflectiveQuadTo -> PathCommand.ReflectiveQuadTo(x, y)
  is PathNode.RelativeReflectiveQuadTo -> PathCommand.RelativeReflectiveQuadTo(dx, dy)
  is PathNode.ArcTo -> PathCommand.ArcTo(
    horizontalEllipseRadius = horizontalEllipseRadius,
    verticalEllipseRadius = verticalEllipseRadius,
    theta = theta,
    isMoreThanHalf = isMoreThanHalf,
    isPositiveArc = isPositiveArc,
    arcStartX = arcStartX,
    arcStartY = arcStartY,
  )
  is PathNode.RelativeArcTo -> PathCommand.RelativeArcTo(
    horizontalEllipseRadius = horizontalEllipseRadius,
    verticalEllipseRadius = verticalEllipseRadius,
    theta = theta,
    isMoreThanHalf = isMoreThanHalf,
    isPositiveArc = isPositiveArc,
    arcStartDx = arcStartDx,
    arcStartDy = arcStartDy,
  )
}

private fun PathCommand.toPathNode(): PathNode = when (this) {
  is PathCommand.Close -> PathNode.Close
  is PathCommand.MoveTo -> PathNode.MoveTo(x, y)
  is PathCommand.RelativeMoveTo -> PathNode.RelativeMoveTo(dx, dy)
  is PathCommand.LineTo -> PathNode.LineTo(x, y)
  is PathCommand.RelativeLineTo -> PathNode.RelativeLineTo(dx, dy)
  is PathCommand.HorizontalTo -> PathNode.HorizontalTo(x)
  is PathCommand.RelativeHorizontalTo -> PathNode.RelativeHorizontalTo(dx)
  is PathCommand.VerticalTo -> PathNode.VerticalTo(y)
  is PathCommand.RelativeVerticalTo -> PathNode.RelativeVerticalTo(dy)
  is PathCommand.CurveTo -> PathNode.CurveTo(x1, y1, x2, y2, x3, y3)
  is PathCommand.RelativeCurveTo -> PathNode.RelativeCurveTo(dx1, dy1, dx2, dy2, dx3, dy3)
  is PathCommand.ReflectiveCurveTo -> PathNode.ReflectiveCurveTo(x1, y1, x2, y2)
  is PathCommand.RelativeReflectiveCurveTo ->
    PathNode.RelativeReflectiveCurveTo(dx1, dy1, dx2, dy2)
  is PathCommand.QuadTo -> PathNode.QuadTo(x1, y1, x2, y2)
  is PathCommand.RelativeQuadTo -> PathNode.RelativeQuadTo(dx1, dy1, dx2, dy2)
  is PathCommand.ReflectiveQuadTo -> PathNode.ReflectiveQuadTo(x, y)
  is PathCommand.RelativeReflectiveQuadTo -> PathNode.RelativeReflectiveQuadTo(dx, dy)
  is PathCommand.ArcTo -> PathNode.ArcTo(
    horizontalEllipseRadius = horizontalEllipseRadius,
    verticalEllipseRadius = verticalEllipseRadius,
    theta = theta,
    isMoreThanHalf = isMoreThanHalf,
    isPositiveArc = isPositiveArc,
    arcStartX = arcStartX,
    arcStartY = arcStartY,
  )
  is PathCommand.RelativeArcTo -> PathNode.RelativeArcTo(
    horizontalEllipseRadius = horizontalEllipseRadius,
    verticalEllipseRadius = verticalEllipseRadius,
    theta = theta,
    isMoreThanHalf = isMoreThanHalf,
    isPositiveArc = isPositiveArc,
    arcStartDx = arcStartDx,
    arcStartDy = arcStartDy,
  )
}
