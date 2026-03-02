package com.r0adkll.livewire

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.r0adkll.livewire.client.ConnectionState
import com.r0adkll.livewire.client.LivewireClient
import com.r0adkll.livewire.ui.icons.ChatBubbleFilled
import com.r0adkll.livewire.ui.icons.ChatBubbleOutline
import com.r0adkll.livewire.ui.icons.Connected
import com.r0adkll.livewire.ui.icons.Disconnected
import com.r0adkll.livewire.ui.icons.HomeFilled
import com.r0adkll.livewire.ui.icons.HomeOutlined
import com.r0adkll.livewire.rickandmorty.CharactersScreen
import kotlinx.coroutines.launch
import com.r0adkll.livewire.ui.theme.CustomLivewireTheme
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LivewireApp(
  livewireClient: LivewireClient,
  modifier: Modifier = Modifier,
) {
  DisposableEffect(livewireClient) {
    livewireClient.start()
    onDispose {
      livewireClient.stop()
    }
  }

  CustomLivewireTheme {
    val scope = rememberCoroutineScope()

    val connectionState by livewireClient.server.connectionState.collectAsState()
    val messages = remember { mutableStateListOf<String>() }

    val outgoingNodeSize by livewireClient.server.outgoingLayoutSize.collectAsState()

    LaunchedEffect(Unit) {
      livewireClient.server.incomingMessages.collect { envelope ->
        messages.add("$envelope")
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
              modifier = Modifier.padding(8.dp)
            ) {
              val tint by animateColorAsState(
                if (connectionState == ConnectionState.CONNECTED) Color(0xff118F00) else MaterialTheme.colorScheme.error
              )

              Icon(
                imageVector = if (connectionState == ConnectionState.CONNECTED) Connected else Disconnected,
                contentDescription = null,
                tint = tint,
              )
            }
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
          }
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
      contentWindowInsets = WindowInsets.systemBars
    ) { padding ->
      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
      ) { page ->
        when (page) {
          0 -> MessagePage(
            messages = messages,
            contentPadding = padding,
            modifier = Modifier.padding(16.dp),
          )
          1 -> CharactersScreen(
            contentPadding = padding,
          )
        }
      }
    }
  }
}

@Composable
private fun MessagePage(
  messages: List<String>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    stickyHeader {
      Text(
        text = "Messages",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
      )
    }
    items(messages) { msg ->
      Text(msg, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

fun Long.asReadableBytes(): String {
  val kb = this.toDouble() / 1024.0
  val mb = kb / 1024.0
  val gb = mb / 1024.0
  val tb = gb / 1024.0

  return if (tb >= 1.0) {
    tb.toFloat().toString(2) + " TB"
  } else if (gb >= 1.0) {
    gb.toFloat().toString(2) + " GB"
  } else if (mb >= 1.0) {
    mb.toFloat().toString(2) + " MB"
  } else if (kb >= 1.0) {
    kb.toFloat().toString(2) + " KB"
  } else {
    "$this B"
  }
}

/**
 * Return the float receiver as a string display with numOfDec after the decimal (rounded)
 * (e.g. 35.72 with numOfDec = 1 will be 35.7, 35.78 with numOfDec = 2 will be 35.80)
 *
 * @param numOfDec number of decimal places to show (receiver is rounded to that number)
 * @return the String representation of the receiver up to numOfDec decimal places
 */
fun Float.toString(numOfDec: Int): String {
  if (isNaN() || isInfinite()) return "--"
  val sign = if (this < 0f) "-" else ""
  val thisAbs = abs(this)
  val integerDigits = thisAbs.toInt()
  val floatDigits = ((thisAbs - integerDigits) * 10f.pow(numOfDec)).roundToInt()
  return "$sign$integerDigits.$floatDigits"
}
