package com.livewire

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.livewire.client.ConnectionState
import com.livewire.client.LivewireClient
import com.livewire.overview.OverviewScreen
import com.livewire.rickandmorty.CharactersScreen
import com.livewire.ui.icons.ChatBubbleFilled
import com.livewire.ui.icons.ChatBubbleOutline
import com.livewire.ui.icons.Connected
import com.livewire.ui.icons.DarkMode
import com.livewire.ui.icons.Disconnected
import com.livewire.ui.icons.HomeFilled
import com.livewire.ui.icons.HomeOutlined
import com.livewire.ui.icons.LightMode
import com.livewire.theme.CustomLivewireTheme
import com.livewire.ui.util.asReadableBytes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LivewireApp(
  livewireClient: LivewireClient,
  modifier: Modifier = Modifier,
  isSystemDarkMode: Boolean = isSystemInDarkTheme()
) {
  DisposableEffect(livewireClient) {
    livewireClient.start()
    onDispose {
      livewireClient.stop()
    }
  }

  val connectionState by livewireClient.server.connectionState.collectAsState()
  var isDarkMode by remember { mutableStateOf(isSystemDarkMode) }

  CustomLivewireTheme(
    darkTheme = isDarkMode,
  ) {
    val scope = rememberCoroutineScope()

    val messages = remember { mutableStateListOf<String>() }
    val outgoingNodeSize by livewireClient.server.outgoingLayoutSize.collectAsState()

    LaunchedEffect(Unit) {
      livewireClient.server.incomingMessages.collect { envelope ->
        messages.add("$envelope")
        if (messages.size > MAX_MESSAGE_HISTORY) {
          messages.removeAt(0)
        }
      }
    }

    val sizeHistory = remember { mutableStateListOf<Long>() }

    LaunchedEffect(Unit) {
      livewireClient.server.outgoingLayoutSize.collect { size ->
        sizeHistory.add(size)
        if (sizeHistory.size > MAX_DATA_POINTS) {
          sizeHistory.removeAt(0)
        }
      }
    }

    val pagerState = rememberPagerState(pageCount = { 2 })

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
      topBar = {
        TopAppBar(
          scrollBehavior = scrollBehavior,
          navigationIcon = {
            Box(
              modifier = Modifier.padding(8.dp),
            ) {
              val tint by animateColorAsState(
                if (connectionState == ConnectionState.Connected) Color(0xff118F00) else MaterialTheme.colorScheme.error,
              )

              Icon(
                imageVector = if (connectionState == ConnectionState.Connected) Connected else Disconnected,
                contentDescription = null,
                tint = tint,
              )
            }
          },
          actions = {
            Switch(
              checked = isDarkMode,
              onCheckedChange = { isDarkMode = it },
              thumbContent = {
                Icon(
                  if (isDarkMode) DarkMode else LightMode,
                  contentDescription = null,
                  modifier = Modifier.size(SwitchDefaults.IconSize),
                  tint = if (isDarkMode) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  } else {
                    MaterialTheme.colorScheme.onPrimary
                  }
                )
              },
              modifier = Modifier.padding(end = 8.dp),
            )
          },
          title = {
            Column {
              Text(
                text = "Livewire",
              )
              Text(
                text = "Node Size: ${outgoingNodeSize.asReadableBytes()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
        )
      },
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
            selected = pagerState.currentPage == 0,
            onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
            icon = {
              Icon(
                imageVector = if (pagerState.currentPage == 0) HomeFilled else HomeOutlined,
                contentDescription = null,
              )
            },
            label = { Text("Home") },
          )
          NavigationBarItem(
            selected = pagerState.currentPage == 1,
            onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
            icon = {
              Icon(
                imageVector = if (pagerState.currentPage == 1) ChatBubbleFilled else ChatBubbleOutline,
                contentDescription = null,
              )
            },
            label = { Text("Characters") },
          )
        }
      },
      modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      contentWindowInsets = WindowInsets.systemBars,
    ) { padding ->
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
      ) { page ->
        when (page) {
          0 -> OverviewScreen(
            currentSize = outgoingNodeSize,
            sizeHistory = sizeHistory,
            messages = messages,
            modifier = Modifier.padding(padding),
          )

          1 -> CharactersScreen(
            contentPadding = padding,
          )
        }
      }
    }
  }
}

private const val MAX_DATA_POINTS = 50
private const val MAX_MESSAGE_HISTORY = 100
