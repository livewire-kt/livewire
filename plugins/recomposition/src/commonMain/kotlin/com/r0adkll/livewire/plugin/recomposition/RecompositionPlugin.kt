@file:OptIn(FlowPreview::class)

package com.r0adkll.livewire.plugin.recomposition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.ClickAction
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.layout.RowScope
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.animateContentSize
import com.r0adkll.livewire.ui.modifier.background
import com.r0adkll.livewire.ui.modifier.border
import com.r0adkll.livewire.ui.modifier.clickable
import com.r0adkll.livewire.ui.modifier.clip
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.horizontalScroll
import com.r0adkll.livewire.ui.modifier.padding
import com.r0adkll.livewire.ui.modifier.rotate
import com.r0adkll.livewire.ui.modifier.size
import com.r0adkll.livewire.ui.modifier.thenIf
import com.r0adkll.livewire.ui.modifier.verticalScroll
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.theme.LivewireTheme
import com.r0adkll.livewire.ui.widget.AnimatedVisibility
import com.r0adkll.livewire.ui.widget.HorizontalDivider
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.ResizableSurface
import com.r0adkll.livewire.ui.widget.ResizeAnchor
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextStyle
import kotlinx.coroutines.FlowPreview
import kotlin.math.roundToInt
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class RecompositionPlugin(
  private val alwaysOnSampling: Boolean = false,
) : Plugin {
  override val info: PluginInfo = PluginInfo(
    pluginId = "recomposition",
    iconData = Icons.Compose,
    title = "Recomposition",
  )

  init {
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
    val collapsed = remember(rawRoots, version) { collapse(rawRoots) }

    val expandOverrides = remember { mutableStateMapOf<Any, Boolean>() }
    var selectedKey by remember { mutableStateOf<Any?>(null) }
    val breadcrumbExpansions = remember { mutableStateMapOf<Any, Set<Int>>() }

    val rows = remember(collapsed, expandOverrides, breadcrumbExpansions, version) {
      flattenTree(collapsed, expandOverrides, breadcrumbExpansions)
    }

    val selectedRow = rows.firstOrNull { it.key == selectedKey }

    Row(LivewireModifier.fillMaxSize()) {
      MainContent(
        rows = rows,
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
          .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Composable",
          modifier = LivewireModifier.weight(1f),
          style = TextStyle.LabelSmall,
          color = LivewireTheme.colorScheme.onSurfaceVariant,
        )
        MetricHeader("Recomps")
        MetricHeader("Skips")
        MetricHeader("Child Recomps")
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      Column(
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(),
      ) {
        rows.forEach { row ->
          val isExpanded = expandOverrides[row.key] != false
          val isSelected = selectedKey == row.key

          Row(
            LivewireModifier
              .fillMaxWidth()
              .thenIf(isSelected) {
                background(LivewireTheme.colorScheme.primaryContainer)
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
                    Text(
                      text = breadcrumb,
                      modifier = LivewireModifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                          action = clickAction(key = "expand_crumb_${nodeKey}_$originalIndex") {
                            onBreadcrumbExpansionChanged(nodeKey, breadcrumbExpansions[nodeKey].orEmpty() + originalIndex)
                          },
                        )
                        .background(LivewireTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                      style = TextStyle.BodySmall,
                      color = LivewireTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      " > ",
                      style = TextStyle.BodySmall,
                      color = LivewireTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    row.name,
                    style = TextStyle.BodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                } else if (row.breadcrumbs.isNotEmpty()) {
                  row.breadcrumbs.forEachIndexed { index, breadcrumb ->
                    val originalIndex = row.breadcrumbOriginalIndices.getOrElse(index) { index }
                    Text(
                      text = breadcrumb,
                      modifier = LivewireModifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                          action = clickAction(key = "expand_crumb_${row.key}_$originalIndex") {
                            onBreadcrumbExpansionChanged(row.key, breadcrumbExpansions[row.key].orEmpty() + originalIndex)
                          },
                        )
                        .background(LivewireTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                      style = TextStyle.BodySmall,
                      color = LivewireTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      " > ",
                      style = TextStyle.BodySmall,
                      color = LivewireTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    row.name,
                    style = TextStyle.BodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                } else {
                  Text(
                    row.name,
                    style = TextStyle.BodySmall,
                    color = LivewireTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              if (row.isBreadcrumbRow) {
                Spacer(modifier = LivewireModifier.width(MetricColumnWidth))
                Spacer(modifier = LivewireModifier.width(MetricColumnWidth))
                Spacer(modifier = LivewireModifier.width(MetricColumnWidth))
              } else {
                MetricBadge(
                  count = row.recompositionCount,
                  color = recompositionColor(row.recompositionCount),
                )
                MetricBadge(
                  count = row.skipCount,
                  color = SkipBadgeText,
                  backgroundColor = SkipBadgeBackground,
                )
                MetricBadge(
                  count = row.childRecompositionCount,
                  color = recompositionColor(row.childRecompositionCount),
                )
              }
            }
          }
        }
      }
    }
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
      style = TextStyle.LabelSmall,
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
        style = TextStyle.LabelSmall,
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
    Column(
      modifier = modifier
        .fillMaxSize()
        .verticalScroll(),
    ) {
      Column(
        modifier = LivewireModifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        if (row.breadcrumbs.isNotEmpty()) {
          Text(
            row.breadcrumbs.joinToString(" > "),
            style = TextStyle.LabelSmall,
            color = LivewireTheme.colorScheme.onSurfaceVariant,
          )
        }
        Text(
          row.name,
          style = TextStyle.TitleSmall,
        )
        if (row.isBreadcrumbRow) {
          Text(
            "collapsed container",
            style = TextStyle.LabelSmall,
            color = DetailValueText,
          )
        }
      }

      HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())

      if (!row.isBreadcrumbRow) {
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
            DetailMetricRow("Child Recomps", "${row.childRecompositionCount}")
            DetailMetricRow("Rate", if (row.recompositionRate < 0.1f) "idle" else "${formatOneDecimal(row.recompositionRate)}/s")
          }
        }

        HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())
      }

      if (!row.isBreadcrumbRow) {
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
                      style = TextStyle.LabelSmall,
                      color = InvalidationStateColor,
                    )
                    Text(
                      text = when {
                        elapsed < 1000 -> "${elapsed}ms ago"
                        elapsed < 60000 -> "${elapsed / 1000}s ago"
                        else -> "${elapsed / 60000}m ago"
                      },
                      style = TextStyle.LabelSmall,
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
                        style = TextStyle.BodySmall,
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
              style = TextStyle.BodySmall,
              color = DetailValueText,
            )
          }
        }

        HorizontalDivider(modifier = LivewireModifier.fillMaxWidth())
      }

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
                  style = TextStyle.LabelSmall,
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
                      style = TextStyle.BodySmall,
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
                      style = TextStyle.BodySmall,
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
            style = TextStyle.BodySmall,
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
        style = TextStyle.LabelSmall,
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
        style = TextStyle.BodySmall,
        color = DetailValueText,
      )
      Text(
        value,
        style = TextStyle.BodySmall,
      )
    }
  }

  companion object {
    // TODO: pretty arbitrary colors and counts
    private fun recompositionColor(count: Int): Color = when {
      count < 2 -> Color(0xFF4CAF50)
      count < 10 -> Color(0xFFFFC107)
      count < 100 -> Color(0xFFFF9800)
      else -> Color(0xFFFF5252)
    }

    // MUST be called prior to the first composition so source information is recorded for every composition.
    fun init() {
      RecompositionTracker.enableSourceInformation()
    }
  }
}

private fun formatOneDecimal(value: Float): String {
  val scaled = (value * 10).roundToInt()
  return "${scaled / 10}.${scaled % 10}"
}

private const val VersionSampleIntervalMs = 100L
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
