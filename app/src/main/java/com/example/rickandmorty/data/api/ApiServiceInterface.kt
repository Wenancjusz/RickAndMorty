package com.example.rickandmorty.data.api

import com.example.rickandmorty.model.ApiResponseModel

interface ApiServiceInterface {
    suspend fun fetchData(page: Int): ApiResult<ApiResponseModel>
}