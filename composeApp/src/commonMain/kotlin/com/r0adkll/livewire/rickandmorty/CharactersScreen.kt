package com.r0adkll.livewire.rickandmorty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory

@Composable
fun CharactersScreen(
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val api = remember { RickAndMortyApi() }

  setSingletonImageLoaderFactory { context ->
    ImageLoader.Builder(context)
      .components {
        add(KtorNetworkFetcherFactory(httpClient = api.httpClient))
      }
      .build()
  }

  var characters by remember { mutableStateOf<List<Character>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  var currentPage by remember { mutableIntStateOf(1) }
  var hasNextPage by remember { mutableStateOf(true) }
  var error by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(currentPage) {
    isLoading = true
    error = null
    try {
      val response = api.getCharacters(currentPage)
      characters = characters + response.results
      hasNextPage = response.info.next != null
    } catch (e: Exception) {
      error = e.message
    }
    isLoading = false
  }

  val gridState = rememberLazyGridState()
  val shouldLoadMore by remember {
    derivedStateOf {
      val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
      lastVisibleItem >= characters.size - 6 && !isLoading && hasNextPage && error == null
    }
  }

  LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore) {
      currentPage++
    }
  }

  if (characters.isEmpty() && isLoading) {
    Box(
      modifier = modifier.fillMaxSize().padding(contentPadding),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator()
    }
    return
  }

  if (characters.isEmpty() && error != null) {
    Box(
      modifier = modifier.fillMaxSize().padding(contentPadding),
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = error ?: "Unknown error",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { currentPage = 1 }) {
          Text("Retry")
        }
      }
    }
    return
  }

  LazyVerticalGrid(
    state = gridState,
    columns = GridCells.Fixed(2),
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
  ) {
    items(characters, key = { it.id }) { character ->
      CharacterCard(character)
    }

    if (isLoading) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

@Composable
private fun CharacterCard(
  character: Character,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column {
      AsyncImage(
        model = character.image,
        contentDescription = character.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
      )

      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = character.name,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(character.statusColor),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = "${character.status} - ${character.species}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

private val Character.statusColor: Color
  get() = when (status.lowercase()) {
    "alive" -> Color(0xFF4CAF50)
    "dead" -> Color(0xFFF44336)
    else -> Color(0xFF9E9E9E)
  }
