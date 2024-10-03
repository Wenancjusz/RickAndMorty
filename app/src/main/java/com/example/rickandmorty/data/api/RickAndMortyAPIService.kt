package com.example.rickandmorty.data.api

import com.example.rickandmorty.model.ApiResponseModel
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query

class RickAndMortyAPIService() : ApiServiceInterface {
    companion object {
        private val baseURL = "https://rickandmortyapi.com/api"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private val service = retrofit.create<CharacterApiServiceInterface>()

    private val rickAndMortyApi: CharacterApiServiceInterface =
        retrofit.create(CharacterApiServiceInterface::class.java)

    override suspend fun fetchData(page: Int): ApiResult<ApiResponseModel> {
        return try {
            val response = rickAndMortyApi.getCharacters(page)
            if (response.isSuccessful) {
                val responseBody = response.body()

                if (responseBody != null) {
                    ApiResult.Success(responseBody)
                } else {
                    ApiResult.Error("API Error: Empty Response Body")
                }

            } else {
                val errorMessage = "Unknown Error"
                ApiResult.Error("API Error: ${response.code()} - $errorMessage")
            }

        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message ?: "Unknown Error"}")
        }
    }

}

interface CharacterApiServiceInterface {
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): Response<ApiResponseModel>
}