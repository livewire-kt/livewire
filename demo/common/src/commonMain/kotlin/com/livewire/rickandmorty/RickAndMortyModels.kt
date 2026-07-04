package com.livewire.rickandmorty

import kotlinx.serialization.Serializable

@Serializable
data class CharacterResponse(
  val info: PageInfo,
  val results: List<Character>,
)

@Serializable
data class PageInfo(
  val count: Int,
  val pages: Int,
  val next: String?,
  val prev: String?,
)

@Serializable
data class Character(
  val id: Int,
  val name: String,
  val status: String,
  val species: String,
  val type: String,
  val gender: String,
  val origin: Location,
  val location: Location,
  val image: String,
  val episode: List<String>,
  val url: String,
  val created: String,
)

@Serializable
data class Location(
  val name: String,
  val url: String,
)
