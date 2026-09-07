@file:OptIn(FlowPreview::class)

package com.livewire.plugin.recomposition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.livewire.ui.Plugin
import com.livewire.ui.PluginInfo
import com.livewire.ui.actions.ClickAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.sizeChangeAction
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.layout.RowScope
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.animateContentSize
import com.livewire.ui.modifier.background
import com.livewire.ui.modifier.border
import com.livewire.ui.modifier.clickable
import com.livewire.ui.modifier.clip
import com.livewire.ui.modifier.fillMaxHeight
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.horizontalScroll
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.rotate
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.thenIf
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.AnimatedVisibility
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.Chip
import com.livewire.ui.widget.ChipStyle
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.ResizableSurface
import com.livewire.ui.widget.ResizeAnchor
import com.livewire.ui.widget.ScrollableColumn
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Text
import kotlin.math.roundToInt
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample

class RecompositionPlugin(
  private val alwaysOnSampling: Boolean = false,
  private val recompositionThresholds: RecompositionThresholds = RecompositionThresholds(),
  private val childRecompositionThresholds: RecompositionThresholds = RecompositionThresholds(),
) : Plugin {
  override val info: PluginInfo = PluginInfo(
    pluginId = "recomposition",
    title = "Recomposition",
    icon = Icons.Compose,
  )

  init {
    // Ensure enableSourceInformation is called before the first composition on non-Android platforms
    init()

    if (alwaysOnSampling) RecompositionTracker.start()
  }

  @Composable
  override fun Content() {
    if (!alwaysOnSampling) {
      DisposableEffect(Unit) {
        RecompositionTracker.start()
        onDispose { RecompositionTracker.stop() }
      }
    }

    val version by remember { RecompositionTracker.version.sample(VersionSampleIntervalMs) }
      .collectAsState(RecompositionTracker.version.value)
    val rawRoots = remember(version) { RecompositionTracker.snapshotRoots() }
    var recomposedOnly by remember { mutableStateOf(false) }
    var showWireframe by remember { mutableStateOf(true) }
    var wireframeHeight by remember { mutableStateOf(InitialWireframeHeight) }
    val collapsed = remember(rawRoots, version, recomposedOnly) {
      collapse(rawRoots).let { if (recomposedOnly) it.onlyRecomposed() else it }
    }

    val expandOverrides = remember { mutableStateMapOf<Any, Boolean>() }
    var selectedKey by remember { mutableStateOf<Any?>(null) }
    val breadcrumbExpansions = remember { mutableStateMapOf<Any, Set<Int>>() }

    val rows by remember(collapsed) {
      derivedStateOf { flattenTree(collapsed, expandOverrides, breadcrumbExpansions) }
    }

    // keeps the "just recomposed" tint decaying while the tracker itself is quiet
    var now by remember { mutableStateOf(MonotonicClock.elapsedMillis()) }
    val hottest = rows.maxOfOrNull { it.lastRecompositionMillis } ?: 0L
    LaunchedEffect(hottest) {
      while (MonotonicClock.elapsedMillis() - hottest < HotWindowMs) {
        delay(HotTickMs)
        now = MonotonicClock.elapsedMillis()
      }
      now = MonotonicClock.elapsedMillis()
    }

    val selectedRow = rows.firstOrNull { it.key == selectedKey }

    Row(LivewireModifier.fillMaxSize()) {
      MainContent(
        rows = rows,
        now = now,
        recomposedOnly = recomposedOnly,
        onRecomposedOnlyChanged = { recomposedOnly = it },
        showWireframe = showWireframe,
        onShowWireframeChanged = { showWireframe = it },
        wireframeHeight = wireframeHeight,
        onWireframeHeightChanged = { wireframeHeight = it },
        expandOverrides = expandOverrides,
        selectedKey = selectedKey,
        breadcrumbExpansions = breadcrumbExpansions,
        onRowSelection = { selectedKey = it },
        onExpandOverrideChanged = { key, isExpanded -> expandOverrides[key] = isExpanded },
        onBreadcrumbExpansionChanged = { nodeKey, updated ->
          if (updated.isEmpty()) {
            breadcrumbExpansions.remove(nodeKey)
          } else {
            breadcrumbExpansions[nodeKey] = updated
          }
        },
      )

      AnimatedVisibility(
        visible = selectedRow != null,
        modifier = LivewireModifier.fillMaxHeight(),
      ) {
        ResizableSurface(
          anchor = ResizeAnchor.Start,
          initialSize = 280.dp,
          maxSize = 500.dp,
          shadowElevation = 2.dp,
          modifier = LivewireModifier.fillMaxHeight(),
        ) {
          selectedRow?.let { DetailPanel(it) }
        }
      }
    }
  }

  @Composable
  private fun RowScope.MainContent(
    rows: List<TreeRow>,
    now: Long,
    recomposedOnly: Boolean,
    onRecomposedOnlyChanged: (Boolean) -> Unit,
    showWireframe: Boolean,
    onShowWireframeChanged: (Boolean) -> Unit,
    wireframeHeight: Dp,
    onWireframeHeightChanged: (Dp) -> Unit,
    expandOverrides: Map<Any, Boolean>,
    selectedKey: Any?,
    breadcrumbExpansions: Map<Any, Set<Int>>,
    onRowSelection: (Any?) -> Unit,
    onExpandOverrideChanged: (Any, Boolean) -> Unit,
    onBreadcrumbExpansionChanged: (Any, Set<Int>) -> Unit,
    modifier: LivewireModifier = LivewireModifier,
  ) {
    Column(
      modifier = modifier
        .weight(1f)
        .fillMaxHeight()
        .animateContentSize(),
    ) {
      Row(
        LivewireModifier
          .fillMaxWidth()
          .background(LivewireTheme.colorScheme.surfaceContainer)
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Chip(
          label = "Recomposed only",
          action = clickAction(key = "recomposed_only") { onRecomposedOnlyChanged(!recomposedOnly) },
          style = ChipStyle.Filter,
          selected = recomposedOnly,
        )
        Spacer(modifier = LivewireModifier.width(8.dp))
        Chip(
          label = "Wireframe",
          action = clickAction(key = "show_wireframe") { onShowWireframeChanged(!showWireframe) },
          style = ChipStyle.Filter,
          selected = showWireframe,
        )
        Spacer(modifier = LivewireModifier.width(8.dp))
        Button(
          action = clickAction(key = "reset_counts") { RecompositionTracker.resetCounts() },
          style = ButtonStyle.Text,
        ) {
          Text("Reset counts")
        }
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      if (showWireframe) {
        ResizableSurface(
          anchor = ResizeAnchor.Bottom,
          initialSize = InitialWireframeHeight,
          minSize = MinWireframeHeight,
          maxSize = MaxWireframeHeight,
          modifier = LivewireModifier.fillMaxWidth(),
          onSizeChange = sizeChangeAction(key = "wireframe_height") { onWireframeHeightChanged(it) },
        ) {
          Wireframe(
            rows = rows,
            selectedKey = selectedKey,
            now = now,
            height = wireframeHeight - WireframePadding * 2,
            onRowSelection = onRowSelection,
          )
        }
        HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())
      }

      Row(
        LivewireModifier
          .fillMaxWidth()
          .background(LivewireTheme.colorScheme.surfaceContainer)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Composable",
          modifier = LivewireModifier.weight(1f),
          style = LivewireTheme.typography.labelSmall,
          color = LivewireTheme.colorScheme.onSurfaceVariant,
        )
        MetricHeader("Recomps")
        MetricHeader("Skips")
        MetricHeader("Child Recomps")
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      ScrollableColumn(
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        rows.forEach { row ->
          val isExpanded = expandOverrides[row.key] != false
          val isSelected = selectedKey == row.key
          val isHot = row.lastRecompositionMillis > 0 && now - row.lastRecompositionMillis < HotWindowMs

          Row(
            LivewireModifier
              .fillMaxWidth()
              .thenIf(isSelected) {
                background(LivewireTheme.colorScheme.primaryContainer)
              }
              .thenIf(isHot && !isSelected) {
                background(HotRowBackground)
              }
              .clickable(
                action = clickAction(key = "select_${row.key}") {
                  onRowSelection(if (isSelected) null else row.key)
                },
              ),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (isSelected) {
              Box(
                modifier = LivewireModifier
                  .width(SelectedIndicatorWidth)
                  .height(20.dp)
                  .clip(RoundedCornerShape(1.dp))
                  .background(LivewireTheme.colorScheme.primary),
              )
            }

            Row(
              modifier = LivewireModifier
                .weight(1f)
                .padding(
                  left = (row.depth * 14).dp + if (isSelected) (RowStartPaddingWidth - SelectedIndicatorWidth) else RowStartPaddingWidth,
                  right = 8.dp,
                  top = 2.dp,
                  bottom = 2.dp,
                ),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              ExpansionIndicatorSlot(
                action = when {
                  row.isBreadcrumbRow -> clickAction(key = "collapse_crumb_${row.key}") {
                    row.breadcrumbNodeKey?.let { nodeKey ->
                      onBreadcrumbExpansionChanged(nodeKey, breadcrumbExpansions[nodeKey].orEmpty() - row.breadcrumbIndex)
                    }
                  }

                  row.hasChildren -> clickAction(key = "toggle_${row.key}") {
                    onExpandOverrideChanged(row.key, !isExpanded)
                  }

                  else -> null
                },
                expanded = isExpanded,
              )

              Row(
                modifier = LivewireModifier
                  .weight(1f)
                  .horizontalScroll(),
              ) {
                if (row.isBreadcrumbRow) {
                  val nodeKey = row.breadcrumbNodeKey ?: row.key
                  row.breadcrumbs.forEachIndexed { index, breadcrumb ->
                    val originalIndex = row.breadcrumbOriginalIndices.getOrElse(index) { index }
                    BreadcrumbChip(
                      name = breadcrumb,
                      count = row.breadcrumbCounts.getOrElse(index) { 0 },
                      action = clickAction(key = "expand_crumb_${nodeKey}_$originalIndex") {
                        onBreadcrumbExpansionChanged(nodeKey, breadcrumbExpansions[nodeKey].orEmpty() + originalIndex)
                      },
                    )
                  }
                  Text(
                    row.name,
                    style = LivewireTheme.typography.bodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                } else if (row.breadcrumbs.isNotEmpty()) {
                  row.breadcrumbs.forEachIndexed { index, breadcrumb ->
                    val originalIndex = row.breadcrumbOriginalIndices.getOrElse(index) { index }
                    BreadcrumbChip(
                      name = breadcrumb,
                      count = row.breadcrumbCounts.getOrElse(index) { 0 },
                      action = clickAction(key = "expand_crumb_${row.key}_$originalIndex") {
                        onBreadcrumbExpansionChanged(row.key, breadcrumbExpansions[row.key].orEmpty() + originalIndex)
                      },
                    )
                  }
                  Text(
                    row.name,
                    style = LivewireTheme.typography.bodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                } else {
                  Text(
                    row.name,
                    style = LivewireTheme.typography.bodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              MetricBadge(
                count = row.recompositionCount,
                color = recompositionColor(row.recompositionCount, recompositionThresholds),
              )
              MetricBadge(
                count = row.skipCount,
                color = SkipBadgeText,
                backgroundColor = SkipBadgeBackground,
              )
              if (row.isBreadcrumbRow) {
                Spacer(modifier = LivewireModifier.width(MetricColumnWidth))
              } else {
                MetricBadge(
                  count = row.childRecompositionCount,
                  color = recompositionColor(row.childRecompositionCount, childRecompositionThresholds),
                )
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun Wireframe(
    rows: List<TreeRow>,
    selectedKey: Any?,
    now: Long,
    height: Dp,
    onRowSelection: (Any?) -> Unit,
  ) {
    val root = rows.firstNotNullOfOrNull { it.bounds }
    if (root == null || root.width <= 0f || root.height <= 0f) {
      Text(
        "No layout bounds captured yet",
        modifier = LivewireModifier.padding(12.dp),
        style = LivewireTheme.typography.bodySmall,
        color = DetailValueText,
      )
      return
    }

    val scale = height.value / root.height
    val canvasWidth = root.width * scale

    Row(modifier = LivewireModifier.fillMaxWidth().horizontalScroll().padding(WireframePadding)) {
      Box(
        modifier = LivewireModifier
          .width(canvasWidth.dp)
          .height(height)
          .clip(RoundedCornerShape(6.dp))
          .background(LivewireTheme.colorScheme.surfaceContainerLow),
      ) {
        rows.take(MaxWireframeRects).forEach { row ->
          val bounds = row.bounds ?: return@forEach
          val left = ((bounds.left - root.left) * scale).coerceIn(0f, canvasWidth)
          val top = ((bounds.top - root.top) * scale).coerceIn(0f, height.value)
          val width = ((bounds.right - root.left) * scale).coerceIn(0f, canvasWidth) - left
          val rectHeight = ((bounds.bottom - root.top) * scale).coerceIn(0f, height.value) - top
          if (width < 1f || rectHeight < 1f) return@forEach

          val isSelected = row.key == selectedKey
          val isHot = row.lastRecompositionMillis > 0 && now - row.lastRecompositionMillis < HotWindowMs
          val color = when {
            isSelected -> LivewireTheme.colorScheme.primary
            row.recompositionCount > 0 -> recompositionColor(row.recompositionCount, recompositionThresholds)
            else -> WireframeIdleOutline
          }
          Box(
            modifier = LivewireModifier
              .padding(left = left.dp, top = top.dp)
              .size(width.dp, rectHeight.dp)
              .border(1.dp, color)
              .thenIf(isSelected) { background(color.copy(alpha = 0.35f)) }
              .thenIf(isHot && !isSelected) { background(HotChipBackground) }
              .clickable(action = clickAction(key = "wire_${row.key}") { onRowSelection(if (isSelected) null else row.key) }),
          ) {
            if (isSelected && width >= MinLabelWidth && rectHeight >= MinLabelHeight) {
              Text(
                text = row.name,
                modifier = LivewireModifier.padding(horizontal = 3.dp, vertical = 1.dp),
                style = LivewireTheme.typography.labelSmall,
                color = LivewireTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
        }
      }
    }
  }

  @Composable
  private fun BreadcrumbChip(
    name: String,
    count: Int,
    action: ClickAction,
  ) {
    Row(
      modifier = LivewireModifier
        .clip(RoundedCornerShape(12.dp))
        .clickable(action = action)
        .background(if (count > 0) HotChipBackground else LivewireTheme.colorScheme.surfaceContainerHigh)
        .padding(horizontal = 8.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = name,
        style = LivewireTheme.typography.bodySmall,
        color = LivewireTheme.colorScheme.onSurfaceVariant,
      )
      if (count > 0) {
        Text(
          text = " $count",
          style = LivewireTheme.typography.labelSmall,
          color = recompositionColor(count, recompositionThresholds),
        )
      }
    }
    Text(
      " > ",
      style = LivewireTheme.typography.bodySmall,
      color = LivewireTheme.colorScheme.onSurfaceVariant,
    )
  }

  @Composable
  private fun MetricHeader(
    text: String,
    modifier: LivewireModifier = LivewireModifier,
  ) {
    // TODO: center align
    Text(
      text = text,
      modifier = modifier
        .width(MetricColumnWidth)
        .padding(horizontal = 8.dp),
      style = LivewireTheme.typography.labelSmall,
      color = LivewireTheme.colorScheme.onSurfaceVariant,
    )
  }

  @Composable
  private fun MetricBadge(
    count: Int,
    color: Color,
    modifier: LivewireModifier = LivewireModifier,
    backgroundColor: Color = color.copy(alpha = 0.15f),
  ) {
    Box(
      modifier = modifier.width(MetricColumnWidth),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "$count",
        LivewireModifier
          .padding(horizontal = 4.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(backgroundColor)
          .padding(horizontal = 6.dp, vertical = 1.dp),
        style = LivewireTheme.typography.labelSmall,
        color = color,
      )
    }
  }

  @Composable
  private fun ExpansionIndicatorSlot(
    action: ClickAction?,
    expanded: Boolean,
  ) {
    if (action != null) {
      IconButton(
        action = action,
        modifier = LivewireModifier
          .size(ExpandIconWidth)
          .rotate(if (expanded) 90f else 0f),
      ) {
        Icon(Icons.ChevronRight)
      }
    } else {
      Spacer(modifier = LivewireModifier.size(ExpandIconWidth))
    }
  }

  @Composable
  private fun DetailPanel(
    row: TreeRow,
    modifier: LivewireModifier = LivewireModifier,
  ) {
    ScrollableColumn(
      modifier = modifier
        .fillMaxSize(),
    ) {
      Column(
        modifier = LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        if (row.breadcrumbs.isNotEmpty()) {
          Text(
            row.breadcrumbs.joinToString(" > "),
            style = LivewireTheme.typography.labelSmall,
            color = LivewireTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          row.name,
          style = LivewireTheme.typography.titleSmall,
        )
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      DetailSection("Metrics") {
        Column(
          modifier = LivewireModifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CardBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
          DetailMetricRow("Recompositions", "${row.recompositionCount}")
          DetailMetricRow("Skips", "${row.skipCount}")
          if (!row.isBreadcrumbRow) {
            DetailMetricRow("Child Recomps", "${row.childRecompositionCount}")
            DetailMetricRow("Rate", if (row.recompositionRate < 0.1f) "idle" else "${formatOneDecimal(row.recompositionRate)}/s")
          }
        }
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      DetailSection("Recent Invalidations") {
        if (row.invalidationReasons.isNotEmpty()) {
          for (reason in row.invalidationReasons.reversed()) {
            Row(
              modifier = LivewireModifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(InvalidationChipBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(
                modifier = LivewireModifier
                  .weight(1f)
                  .padding(left = 8.dp),
              ) {
                Row(
                  modifier = LivewireModifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  val elapsed = MonotonicClock.elapsedMillis() - reason.timestamp

                  Text(
                    text = reason.label,
                    modifier = LivewireModifier.weight(1f),
                    style = LivewireTheme.typography.labelSmall,
                    color = InvalidationStateColor,
                  )
                  Text(
                    text = when {
                      elapsed < 1000 -> "${elapsed}ms ago"
                      elapsed < 60000 -> "${elapsed / 1000}s ago"
                      else -> "${elapsed / 60000}m ago"
                    },
                    style = LivewireTheme.typography.labelSmall,
                    color = TimestampText,
                  )
                }
                if (reason.value != null) {
                  val isLongValue = reason.value.length > ScrollableContainerThreshold
                  Column(
                    modifier = LivewireModifier
                      .fillMaxWidth()
                      .padding(top = 2.dp)
                      .thenIf(isLongValue) {
                        height(ScrollableContainerHeight).verticalScroll()
                      },
                  ) {
                    Text(
                      reason.value,
                      style = LivewireTheme.typography.bodySmall,
                      color = DetailValueText,
                    )
                  }
                }
              }
            }
          }
        } else {
          Text(
            "No invalidations recorded",
            style = LivewireTheme.typography.bodySmall,
            color = DetailValueText,
          )
        }
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      DetailSection("Parameters") {
        if (row.parameters.isNotEmpty()) {
          for (param in row.parameters) {
            Column(
              modifier = LivewireModifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CardBackground)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
              Row(
                modifier = LivewireModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  param.name,
                  style = LivewireTheme.typography.labelSmall,
                  color = ParamNameColor,
                )
              }

              when (val value = param.value) {
                is ParameterValue.ColorValue -> {
                  Row(
                    modifier = LivewireModifier
                      .fillMaxWidth()
                      .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    if (value.color != Color.Unspecified) {
                      Box(
                        modifier = LivewireModifier
                          .size(12.dp)
                          .clip(RoundedCornerShape(2.dp))
                          .border(0.dp, Color.Black)
                          .background(value.color),
                      )
                      Spacer(modifier = LivewireModifier.padding(left = 4.dp))
                    }
                    Text(
                      value.displayValue,
                      style = LivewireTheme.typography.bodySmall,
                      color = DetailValueText,
                    )
                  }
                }

                else -> {
                  val displayText = value.displayValue
                  val isLongValue = displayText.length > ScrollableContainerThreshold
                  Column(
                    modifier = LivewireModifier
                      .fillMaxWidth()
                      .padding(top = 2.dp)
                      .thenIf(isLongValue) {
                        height(ScrollableContainerHeight).verticalScroll()
                      },
                  ) {
                    Text(
                      displayText,
                      style = LivewireTheme.typography.bodySmall,
                      color = DetailValueText,
                    )
                  }
                }
              }
            }
          }
        } else {
          Text(
            text = "No parameters captured",
            style = LivewireTheme.typography.bodySmall,
            color = DetailValueText,
          )
        }
      }
    }
  }

  @Composable
  private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
  ) {
    Column(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
      Text(
        title,
        style = LivewireTheme.typography.labelSmall,
        color = DetailSectionTitle,
      )
      Spacer(modifier = LivewireModifier.padding(top = 2.dp))
      content()
    }
  }

  @Composable
  private fun DetailMetricRow(label: String, value: String) {
    Row(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(top = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        label,
        modifier = LivewireModifier.weight(1f),
        style = LivewireTheme.typography.bodySmall,
        color = DetailValueText,
      )
      Text(
        value,
        style = LivewireTheme.typography.bodySmall,
      )
    }
  }

  companion object {
    private fun recompositionColor(count: Int, thresholds: RecompositionThresholds): Color = when {
      count < thresholds.low -> Color(0xFF4CAF50)
      count < thresholds.moderate -> Color(0xFFFFC107)
      count < thresholds.high -> Color(0xFFFF9800)
      else -> Color(0xFFFF5252)
    }

    // MUST be called prior to the first composition so source information is recorded for every composition.
    fun init() {
      RecompositionTracker.enableSourceInformation()
    }
  }
}

data class RecompositionThresholds(
  val low: Int = 2,
  val moderate: Int = 5,
  val high: Int = 10,
)

private fun formatOneDecimal(value: Float): String {
  val scaled = (value * 10).roundToInt()
  return "${scaled / 10}.${scaled % 10}"
}

private const val VersionSampleIntervalMs = 100L
private const val HotWindowMs = 1500L
private const val MaxWireframeRects = 400
private const val MinLabelWidth = 40f
private const val MinLabelHeight = 14f

private val InitialWireframeHeight = 300.dp
private val MinWireframeHeight = 120.dp
private val MaxWireframeHeight = 1200.dp
private val WireframePadding = 12.dp
private val WireframeIdleOutline = Color(0xFF8E8E93).copy(alpha = 0.45f)
private const val HotTickMs = 250L
private val MetricColumnWidth = 72.dp
private const val ScrollableContainerThreshold = 120
private val ScrollableContainerHeight = 80.dp

private val SkipBadgeBackground = Color(0xFF2A2A2E)
private val SkipBadgeText = Color(0xFF8E8E93)
private val SelectedIndicatorWidth = 3.dp
private val RowStartPaddingWidth = 8.dp
private val ExpandIconWidth = 24.dp

private val DetailSectionTitle = Color(0xFFB0AAB8)
private val DetailValueText = Color(0xFF9590A0)
private val InvalidationStateColor = Color(0xFFFFAB40)
private val InvalidationChipBackground = Color(0xFFFFAB40).copy(alpha = 0.08f)
private val ParamNameColor = Color(0xFF81D4FA)
private val CardBackground = Color(0xFFFFFFFF).copy(alpha = 0.04f)
private val TimestampText = Color(0xFF7A7680)
private val HotRowBackground = Color(0xFFFFAB40).copy(alpha = 0.12f)
private val HotChipBackground = Color(0xFFFFAB40).copy(alpha = 0.25f)
