package com.livewire.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.molecule.moleculeFlow
import com.livewire.annotations.LivewireLayoutSerializer
import com.livewire.ui.actions.LivewireActionObserver
import com.livewire.ui.actions.LocalLivewireActionObserver
import com.livewire.ui.actions.checkedChangeAction
import com.livewire.ui.actions.clickAction
import com.livewire.ui.actions.floatValueChangeAction
import com.livewire.ui.actions.intValueChangeAction
import com.livewire.ui.actions.valueChangeAction
import com.livewire.ui.composition.launchLivewire
import com.livewire.ui.composition.livewireFlow
import com.livewire.ui.data.LayoutNodeSerializers
import com.livewire.ui.data.LivewireUiJson
import com.livewire.ui.data.LivewireUiProtobuf
import com.livewire.ui.graphics.CircleShape
import com.livewire.ui.graphics.RoundedCornerShape
import com.livewire.ui.layout.Alignment
import com.livewire.ui.layout.Box
import com.livewire.ui.layout.Column
import com.livewire.ui.layout.RootNode
import com.livewire.ui.layout.Row
import com.livewire.ui.modifier.LivewireModifier
import com.livewire.ui.modifier.fillMaxSize
import com.livewire.ui.modifier.fillMaxWidth
import com.livewire.ui.modifier.height
import com.livewire.ui.modifier.padding
import com.livewire.ui.modifier.size
import com.livewire.ui.modifier.verticalScroll
import com.livewire.ui.modifier.width
import com.livewire.ui.widget.AnimatedVisibility
import com.livewire.ui.widget.Button
import com.livewire.ui.widget.ButtonSize
import com.livewire.ui.widget.ButtonStyle
import com.livewire.ui.widget.Checkbox
import com.livewire.ui.widget.DropdownMenu
import com.livewire.ui.widget.DropdownMenuItem
import com.livewire.ui.widget.FabSize
import com.livewire.ui.widget.FloatingActionButton
import com.livewire.ui.widget.FloatingToolbar
import com.livewire.ui.widget.HorizontalDivider
import com.livewire.ui.widget.Icon
import com.livewire.ui.widget.IconButton
import com.livewire.ui.widget.IconButtonStyle
import com.livewire.ui.widget.ProgressIndicator
import com.livewire.ui.widget.ProgressIndicatorStyle
import com.livewire.ui.widget.RadioButton
import com.livewire.ui.widget.Slider
import com.livewire.ui.widget.Spacer
import com.livewire.ui.widget.Surface
import com.livewire.ui.widget.Switch
import com.livewire.ui.widget.Tab
import com.livewire.ui.widget.TabRow
import com.livewire.ui.widget.Table
import com.livewire.ui.widget.Text
import com.livewire.ui.widget.TextField
import com.livewire.ui.widget.TextFieldStyle
import com.livewire.ui.widget.ToggleButton
import com.livewire.ui.widget.VerticalDivider
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class LayoutNodeSerializationTest {

  // TODO: ignored because it's currently hanging forever
  @Ignore
  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun testJsonVsCborLayoutNodeSerialization() = runTest {
    val json = LivewireUiJson
    val protobuf = LivewireUiProtobuf

    // TODO: This is broke AF and doesn't "stop" the coroutine.
    val layoutNode = launchLivewire(
      mode = RecompositionMode.Immediate,
    ) {
      CompositionLocalProvider(
        LocalLivewireActionObserver provides LivewireActionObserver.NoOp,
      ) {
        TestContent()
      }
    }.firstOrNull { it.children.isNotEmpty() }

    val jsonResultString = json.encodeToString(layoutNode)
    println(jsonResultString)

    val jsonResult = jsonResultString.encodeToByteArray()
    val protobufResult = protobuf.encodeToByteArray(layoutNode)

    println("json size     : ${jsonResult.size}")
    println("protobuf size : ${protobufResult.size}")

    assertTrue {
      protobufResult.size < jsonResult.size
    }
  }
}

@Composable
fun TestContent() {
  Column(LivewireModifier.fillMaxSize()) {

    // Buttons
    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      Button(
        action = clickAction {

        }
      ) {
        Text("Filled")
      }

      Button(
        action = clickAction {

        },
        style = ButtonStyle.Tonal,
      ) {
        Text("Tonal")
      }

      Button(
        action = clickAction {

        },
        style = ButtonStyle.Outlined,
      ) {
        Text("Outlined")
      }

      Button(
        action = clickAction {

        },
        style = ButtonStyle.Elevated,
      ) {
        Text("Elevated")
      }

      Button(
        action = clickAction {

        },
        style = ButtonStyle.Text,
      ) {
        Text("Text")
      }

      IconButton(
        action = clickAction {

        },
      ) { Icon(Icons.Sync) }

      IconButton(
        action = clickAction {

        },
        style = IconButtonStyle.Filled
      ) { Icon(Icons.Sync) }

      IconButton(
        action = clickAction {

        },
        style = IconButtonStyle.Tonal
      ) { Icon(Icons.Sync) }

      IconButton(
        action = clickAction {

        },
        style = IconButtonStyle.Outlined
      ) { Icon(Icons.Sync) }

      var checked by remember { mutableStateOf(false) }
      ToggleButton(
        checked = checked,
        onCheckedChange = checkedChangeAction {
          checked = it
        }
      ) {
        Icon(Icons.Sync)
        Text(
          if (checked) "On" else "Off",
        )
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Button(
        action = clickAction {

        },
        size = ButtonSize.Large,
      ) {
        Icon(Icons.Sync)
        Text("Large")
      }
      Button(
        action = clickAction {

        },
        size = ButtonSize.Medium,
      ) {
        Icon(Icons.Sync)
        Text("Medium")
      }
      Button(
        action = clickAction {

        },
        size = ButtonSize.Small,
      ) {
        Icon(Icons.Sync)
        Text("Small")
      }
      Button(
        action = clickAction {

        },
        size = ButtonSize.ExtraSmall,
      ) {
        Icon(Icons.Sync)
        Text("X-Small")
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      FloatingActionButton(
        action = clickAction { },
        size = FabSize.Small,
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        Icon(Icons.Sync)
      }

      FloatingActionButton(
        action = clickAction { },
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        Icon(Icons.Sync)
      }

      FloatingActionButton(
        action = clickAction { },
        size = FabSize.Large,
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        Icon(Icons.Sync)
      }

      FloatingActionButton(
        action = clickAction { },
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        Icon(Icons.Sync)
        Text("Extended")
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      var checked by remember { mutableStateOf(false) }
      Checkbox(
        checked = checked,
        onCheckedChange = checkedChangeAction {
          checked = it
        }
      )

      RadioButton(
        selected = checked,
        onClick = clickAction {
          checked = !checked
        }
      )

      var switchChecked by remember { mutableStateOf(true) }
      Switch(
        checked = switchChecked,
        onCheckedChange = checkedChangeAction {
          switchChecked = it
        },
      )

      Switch(
        checked = false,
        onCheckedChange = checkedChangeAction { },
        enabled = false,
      )
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      ProgressIndicator(
        modifier = LivewireModifier
          .width(200.dp)
          .padding(horizontal = 16.dp),
      )

      ProgressIndicator(
        progress = 0.5f,
        modifier = LivewireModifier
          .width(200.dp)
          .padding(horizontal = 16.dp),
      )

      ProgressIndicator(
        style = ProgressIndicatorStyle.Circular,
        modifier = LivewireModifier
          .padding(horizontal = 16.dp),
      )

      ProgressIndicator(
        progress = 0.5f,
        style = ProgressIndicatorStyle.Circular,
        modifier = LivewireModifier
          .padding(horizontal = 16.dp),
      )
    }


    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      TextField(
        initialValue = "",
        onValueChange = valueChangeAction {  },
        modifier = LivewireModifier
          .weight(1f)
          .padding(16.dp)
      )

      TextField(
        initialValue = "",
        onValueChange = valueChangeAction {  },
        style = TextFieldStyle.Outlined,
        modifier = LivewireModifier
          .weight(1f)
          .padding(16.dp)
      )

    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      var sliderValue by remember { mutableStateOf(0.5f) }
      Slider(
        value = sliderValue,
        onValueChange = floatValueChangeAction {
          sliderValue = it
        },
        modifier = LivewireModifier
          .weight(1f)
          .padding(horizontal = 16.dp),
      )

      var steppedValue by remember { mutableStateOf(0f) }
      Slider(
        value = steppedValue,
        onValueChange = floatValueChangeAction {
          steppedValue = it
        },
        steps = 4,
        modifier = LivewireModifier
          .weight(1f)
          .padding(horizontal = 16.dp),
      )

      Slider(
        value = 0.3f,
        onValueChange = floatValueChangeAction { },
        enabled = false,
        modifier = LivewireModifier
          .weight(1f)
          .padding(horizontal = 16.dp),
      )
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      Surface(
        modifier = LivewireModifier
          .size(150.dp, 80.dp)
          .padding(horizontal = 16.dp),
      ) {
        Text("Flat")
      }

      Surface(
        modifier = LivewireModifier
          .size(150.dp, 80.dp)
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
      ) {
        Text("Rounded")
      }

      Surface(
        modifier = LivewireModifier
          .size(150.dp, 80.dp)
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
      ) {
        Text("Shadow")
      }

      Surface(
        modifier = LivewireModifier
          .size(150.dp, 80.dp)
          .padding(horizontal = 16.dp),
        shape = CircleShape,
        tonalElevation = 2.dp,
      ) {
        Text("Circle")
      }

      Surface(
        modifier = LivewireModifier
          .size(150.dp, 80.dp)
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        onClick = clickAction { },
      ) {
        Text("Click")
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {

      FloatingToolbar(
        expanded = true,
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
      }

      FloatingToolbar(
        expanded = false,
        modifier = LivewireModifier.padding(horizontal = 16.dp),
      ) {
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
        IconButton(
          action = clickAction { },
        ) { Icon(Icons.Sync) }
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      var menuExpanded by remember { mutableStateOf(false) }

      Box {
        Button(
          action = clickAction {
            menuExpanded = true
          },
        ) {
          Text("Show Menu")
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = clickAction {
            menuExpanded = false
          },
        ) {
          DropdownMenuItem(
            text = "Option 1",
            onClick = clickAction { menuExpanded = false },
            leadingIcon = Icons.Sync,
          )
          DropdownMenuItem(
            text = "Option 2",
            onClick = clickAction { menuExpanded = false },
          )
          DropdownMenuItem(
            text = "Disabled",
            onClick = clickAction { },
            enabled = false,
          )
        }
      }
    }

    Row(
      LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Left")
      Spacer(modifier = LivewireModifier.width(48.dp))
      Text("Center")
      Spacer(modifier = LivewireModifier.width(48.dp))
      Text("Right")
    }

    // Dividers
    HorizontalDivider(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )

    Row(
      LivewireModifier
        .fillMaxWidth()
        .height(48.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Left")
      VerticalDivider(modifier = LivewireModifier.padding(horizontal = 16.dp))
      Text("Center")
      VerticalDivider(modifier = LivewireModifier.padding(horizontal = 16.dp))
      Text("Right")
    }

    HorizontalDivider(
      modifier = LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      thickness = 3.dp,
    )

    Column(
      modifier = LivewireModifier
        .height(200.dp)
        .verticalScroll()
        .padding(horizontal = 16.dp),
    ) {
      repeat(20) {
        Text("Item $it")
      }
    }

    // TabRow
    var selectedTab by remember { mutableStateOf(0) }
    TabRow(
      selectedTabIndex = selectedTab,
      onTabSelected = intValueChangeAction {
        selectedTab = it
      },
      modifier = LivewireModifier.fillMaxWidth(),
    ) {
      Tab(text = "Overview")
      Tab(text = "Details")
      Tab(text = "Settings")
    }

    // AnimatedVisibility
    Row(
      LivewireModifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      var contentVisible by remember { mutableStateOf(true) }
      Button(
        action = clickAction {
          contentVisible = !contentVisible
        },
      ) {
        Text(if (contentVisible) "Hide" else "Show")
      }

      AnimatedVisibility(visible = contentVisible) {
        Text("Hello, AnimatedVisibility!")
      }
    }

    // Table
    Table(
      columns = listOf("Name", "Role", "Status"),
      rows = listOf(
        listOf("Alice", "Engineer", "Active"),
        listOf("Bob", "Designer", "Active"),
        listOf("Charlie", "PM", "Away"),
        listOf("Diana", "Engineer", "Active"),
        listOf("Eve", "QA", "Offline"),
        listOf("Frank", "DevOps", "Active"),
        listOf("Grace", "Designer", "Away"),
        listOf("Hank", "Engineer", "Active"),
        listOf("Iris", "PM", "Active"),
        listOf("Jack", "QA", "Offline"),
        listOf("Karen", "Engineer", "Away"),
        listOf("Leo", "Designer", "Active"),
        listOf("Mona", "DevOps", "Active"),
        listOf("Nate", "Engineer", "Offline"),
        listOf("Olivia", "PM", "Active"),
      ),
      pageSize = 5,
      modifier = LivewireModifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp),
    )

  }
}

internal object Icons {

  internal val Sync: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
      name = "Sync",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 960f,
      viewportHeight = 960f,
    ).apply {
      path(fill = SolidColor(Color.Black)) {
        moveTo(240f, 482f)
        quadToRelative(0f, 45f, 17f, 87.5f)
        reflectiveQuadToRelative(53f, 78.5f)
        lineToRelative(10f, 10f)
        verticalLineToRelative(-58f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(360f, 560f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(400f, 600f)
        verticalLineToRelative(160f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(360f, 800f)
        horizontalLineTo(200f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(160f, 760f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(200f, 720f)
        horizontalLineToRelative(70f)
        lineToRelative(-16f, -14f)
        quadToRelative(-52f, -46f, -73f, -105f)
        reflectiveQuadToRelative(-21f, -119f)
        quadToRelative(0f, -94f, 48f, -170.5f)
        reflectiveQuadTo(337f, 194f)
        quadToRelative(14f, -8f, 29.5f, -1f)
        reflectiveQuadToRelative(20.5f, 23f)
        quadToRelative(5f, 15f, -0.5f, 30f)
        reflectiveQuadTo(367f, 269f)
        quadToRelative(-58f, 32f, -92.5f, 88.5f)
        reflectiveQuadTo(240f, 482f)
        close()
        moveToRelative(480f, -4f)
        quadToRelative(0f, -45f, -17f, -87.5f)
        reflectiveQuadTo(650f, 312f)
        lineToRelative(-10f, -10f)
        verticalLineToRelative(58f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(600f, 400f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(560f, 360f)
        verticalLineToRelative(-160f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(600f, 160f)
        horizontalLineToRelative(160f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(800f, 200f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(760f, 240f)
        horizontalLineToRelative(-70f)
        lineToRelative(16f, 14f)
        quadToRelative(49f, 49f, 71.5f, 106.5f)
        reflectiveQuadTo(800f, 478f)
        quadToRelative(0f, 94f, -48f, 170.5f)
        reflectiveQuadTo(623f, 766f)
        quadToRelative(-14f, 8f, -29.5f, 1f)
        reflectiveQuadTo(573f, 744f)
        quadToRelative(-5f, -15f, 0.5f, -30f)
        reflectiveQuadToRelative(19.5f, -23f)
        quadToRelative(58f, -32f, 92.5f, -88.5f)
        reflectiveQuadTo(720f, 478f)
        close()
      }
    }.build()
  }
}
