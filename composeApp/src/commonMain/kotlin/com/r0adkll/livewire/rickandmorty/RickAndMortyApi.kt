package com.r0adkll.livewire.rickandmorty

import com.r0adkll.livewire.plugin.network.ktor.LivewireNetworkPlugin
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RickAndMortyApi {

  val httpClient = HttpClient {
    install(ContentNegotiation) {
      json(Json { ignoreUnknownKeys = true })
    }
    install(LivewireNetworkPlugin)
  }

  suspend fun getCharacters(page: Int = 1): CharacterResponse {
    return httpClient.get("https://rickandmortyapi.com/api/character?page=$page").body()
  }

  suspend fun getCharacter(id: Int): Character {
    return httpClient.get("https://rickandmortyapi.com/api/character/$id").body()
  }
}
