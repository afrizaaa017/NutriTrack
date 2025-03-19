package com.example.nutritrack.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.Response
import com.example.nutritrack.data.model.FoodResponse

interface ApiService {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Response<FoodResponse>
}