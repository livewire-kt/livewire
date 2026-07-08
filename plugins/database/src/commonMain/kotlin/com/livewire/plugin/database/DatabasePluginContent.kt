package com.livewire.plugin.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.livewire.plugin.database.composables.DatabaseToolBar
import com.livewire.plugin.database.composables.TableList
import com.livewire.plugin.database.data.DatabaseInspector
import com.livewire.plugin.database.ui.DatabaseSearch
import com.livewire.plugin.database.ui.Icons
import com.livewire.plugin.database.ui.Run
import com.livewire.plugin.database.ui.Schema
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.intValueChangeAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.animateContentSize
import com.livewire.ui.modifier.background
import com.livewire.ui.modifier.fillMaxHeight
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.width
import com.livewire.ui.theme.LivewireTheme
import com.livewire.ui.widget.AnimatedVisibility
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonShapes
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.IconButtonStyle
import com.livewire.ui.widget.ResizableSurface
import com.livewire.ui.widget.ResizeAnchor
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Tab
import com.livewire.ui.widget.TabRow
import com.livewire.ui.widget.Table
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextField

@Composable
internal fun DatabasePluginContent(inspector: DatabaseInspector) {
  val presenter = remember { DatabasePresenter(inspector) }
  val state = presenter.present()

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var showSchema by remember { mutableStateOf(true) }

  Row(
    modifier = LivewireModifier
      .fillMaxSize(),
  ) {
    Column(
      modifier = LivewireModifier
        .weight(1f)
        .fillMaxHeight()
        .animateContentSize(),
    ) {
      DatabaseToolBar(
        selectedDatabase = state.selectedDatabase,
        availableDatabases = state.availableDatabases,
        onDatabaseSelected = {
          state.eventSink(DatabaseUiEvent.SelectDatabase(it))
        },
        tabs = {
          TabRow(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = intValueChangeAction {
              selectedTabIndex = it
            },
            modifier = LivewireModifier.fillMaxWidth(),
          ) {
            state.pages.forEach { page ->
              Tab(text = page.name)
            }
          }
        },
        actions = {
          Button(
            action = clickAction {
              state.eventSink(DatabaseUiEvent.AddQueryTab)
            },
            size = ButtonSize.ExtraSmall,
            style = ButtonStyle.Tonal,
            shapes = ButtonShapes(
              shape = RoundedCornerShape(8.dp),
              pressedShape = CircleShape,
            ),
          ) {
            Icon(Icons.DatabaseSearch)
            Text("New Query")
          }

          Spacer(LivewireModifier.width(8.dp))

          IconButton(
            action = clickAction {
              showSchema = !showSchema
            },
          ) {
            Icon(Icons.Schema)
          }
        },
      )

      Column(
        modifier = LivewireModifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        state.pages.getOrNull(selectedTabIndex)?.let { page ->
          when (page) {
            is QueryPage -> QueryContentPage(
              index = selectedTabIndex,
              page = page,
              onQueryChange = { query ->
                state.eventSink(DatabaseUiEvent.UpdateQueryForTab(selectedTabIndex, query))
              },
              onExecute = {
                state.eventSink(DatabaseUiEvent.ExecuteQueryForTab(selectedTabIndex))
              },
              modifier = LivewireModifier.fillMaxSize(),
            )

            is TableContentPage -> TableContentPage(
              page = page,
              modifier = LivewireModifier.fillMaxSize(),
            )
          }
        }
      }
    }


    AnimatedVisibility(
      visible = state.selectedDatabase != null && showSchema,
      modifier = LivewireModifier.fillMaxHeight(),
    ) {
      ResizableSurface(
        anchor = ResizeAnchor.Start,
        initialSize = 300.dp,
        maxSize = 600.dp,
        shadowElevation = 2.dp,
        modifier = LivewireModifier
          .fillMaxHeight(),
      ) {
        TableList(
          tables = state.selectedDatabaseTables,
          onTableClick = {
            state.eventSink(DatabaseUiEvent.SelectTable(it))
          },
          modifier = LivewireModifier
            .weight(2f)
            .fillMaxHeight(),
        )
      }
    }
  }
}

@Composable
private fun TableContentPage(
  page: TableContentPage,
  modifier: LivewireModifier = LivewireModifier,
) {
  page.content?.let { content ->
    Table(
      columns = content.columns,
      rows = content.rows.map { rows -> rows.map { it ?: "null" } },
      pageSize = 25,
      modifier = modifier,
    )
  }
}

@Composable
private fun QueryContentPage(
  index: Int,
  page: QueryPage,
  onQueryChange: (String) -> Unit,
  onExecute: () -> Unit,
  modifier: LivewireModifier = LivewireModifier,
) = key(index) {
  Column(
    modifier = modifier,
  ) {
    Row(
      modifier = LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextField(
        initialValue = page.query,
        onValueChange = valueChangeAction {
          onQueryChange(it)
        },
        label = "Query",
        placeholder = "Enter your SQL query here\u2026",
        modifier = LivewireModifier
          .weight(1f)
          .height(128.dp),
      )
      Box(
        modifier = LivewireModifier
          .background(LivewireTheme.colorScheme.surfaceContainerHighest)
          .height(128.dp)
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
      ) {
        IconButton(
          action = clickAction {
            onExecute()
          },
          size = ButtonSize.Medium,
          style = IconButtonStyle.Filled,
        ) {
          Icon(Icons.Run)
        }
      }
    }

    HorizontalDivider(LivewireModifier.fillMaxWidth())

    Box(LivewireModifier.weight(1f)) {
      page.result?.let { result ->
        Table(
          columns = result.columns,
          rows = result.rows.map { rows -> rows.map { it ?: "null" } },
          pageSize = 25,
          modifier = LivewireModifier.fillMaxSize(),
        )
      }
    }
  }
}
