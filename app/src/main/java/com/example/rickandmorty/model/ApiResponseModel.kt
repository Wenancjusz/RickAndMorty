package com.example.rickandmorty.model

data class ApiResponseModel(
    val info: InfoModel,
    val results: List<CharacterModel>
)
