package com.r0adkll.livewire.plugin.database

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.plugin.database.composables.DatabaseToolBar
import com.r0adkll.livewire.plugin.database.composables.TableList
import com.r0adkll.livewire.plugin.database.data.DatabaseInspector
import com.r0adkll.livewire.plugin.database.ui.Icons
import com.r0adkll.livewire.ui.Plugin
import com.r0adkll.livewire.ui.PluginInfo
import com.r0adkll.livewire.ui.actions.clickAction
import com.r0adkll.livewire.ui.actions.intValueChangeAction
import com.r0adkll.livewire.ui.actions.valueChangeAction
import com.r0adkll.livewire.ui.graphics.CircleShape
import com.r0adkll.livewire.ui.graphics.RoundedCornerShape
import com.r0adkll.livewire.ui.layout.Alignment
import com.r0adkll.livewire.ui.layout.Box
import com.r0adkll.livewire.ui.layout.Column
import com.r0adkll.livewire.ui.layout.Row
import com.r0adkll.livewire.ui.modifier.LivewireModifier
import com.r0adkll.livewire.ui.modifier.animateContentSize
import com.r0adkll.livewire.ui.modifier.fillMaxHeight
import com.r0adkll.livewire.ui.modifier.fillMaxSize
import com.r0adkll.livewire.ui.modifier.fillMaxWidth
import com.r0adkll.livewire.ui.modifier.height
import com.r0adkll.livewire.ui.modifier.width
import com.r0adkll.livewire.ui.widget.AnimatedVisibility
import com.r0adkll.livewire.ui.widget.Button
import com.r0adkll.livewire.ui.widget.ButtonShapes
import com.r0adkll.livewire.ui.widget.ButtonSize
import com.r0adkll.livewire.ui.widget.ButtonStyle
import com.r0adkll.livewire.ui.widget.HorizontalDivider
import com.r0adkll.livewire.ui.widget.Icon
import com.r0adkll.livewire.ui.widget.IconButton
import com.r0adkll.livewire.ui.widget.IconButtonStyle
import com.r0adkll.livewire.ui.widget.Spacer
import com.r0adkll.livewire.ui.widget.Surface
import com.r0adkll.livewire.ui.widget.Tab
import com.r0adkll.livewire.ui.widget.TabRow
import com.r0adkll.livewire.ui.widget.Table
import com.r0adkll.livewire.ui.widget.Text
import com.r0adkll.livewire.ui.widget.TextField

class DatabasePlugin(context: Context) : Plugin {

  private val inspector = DatabaseInspector(context)
  private val presenter = DatabasePresenter(inspector)

  override val info: PluginInfo = PluginInfo(
    pluginId = "database",
    iconData = Icons.Database,
    title = "Database",
  )

  @Composable
  override fun Content() {
    val state = presenter.present()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(LivewireModifier.fillMaxSize()) {
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
            state.pages.forEachIndexed { index, page ->
              Tab(
                text = page.name,
              )
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
            )
          ) {
            Icon(Icons.DatabaseSearch)
            Text("New Query")
          }
        }
      )

      Row(
        modifier = LivewireModifier
          .animateContentSize(),
      ) {

        // Main Content
        Column(
          modifier = LivewireModifier
            .weight(1f)
            .fillMaxHeight()
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

        // Side Content
        AnimatedVisibility(
          visible = state.selectedDatabase != null,
          modifier = LivewireModifier.fillMaxHeight(),
        ) {
          Surface(
            shadowElevation = 2f,
            modifier = LivewireModifier
              .fillMaxHeight()
              .width(300.dp)
          ) {

            TableList(
              tables = state.selectedDatabaseTables,
              onTableClick = {
                state.eventSink(DatabaseUiEvent.SelectTable(it))
              },
              modifier = LivewireModifier
                .weight(2f)
                .fillMaxHeight()
            )

          }
        }
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
      pageSize = 25, // ? Configure this ?
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
        placeholder = "Enter your SQL query here…",
        modifier = LivewireModifier
          .weight(1f)
          .height(128.dp),
      )
      Spacer(LivewireModifier.width(16.dp))
      IconButton(
        action = clickAction {
          onExecute()
        },
        size = ButtonSize.Medium,
        style = IconButtonStyle.Filled,
      ) {
        Icon(Icons.Run)
      }
      Spacer(LivewireModifier.width(16.dp))
    }

    HorizontalDivider(LivewireModifier.fillMaxWidth())

    Box(LivewireModifier.weight(1f)) {
      page.result?.let { result ->
        Table(
          columns = result.columns,
          rows = result.rows.map { rows -> rows.map { it ?: "null" } },
          pageSize = 25,
          modifier = LivewireModifier.fillMaxSize()
        )
      }
    }
  }
}

