package com.example.nutritrack.data.repository

import android.util.Log
import retrofit2.Response
import com.example.nutritrack.data.api.ApiClient
import com.example.nutritrack.data.model.FoodResponse

class FoodRepository  {
    private val apiService = ApiClient.apiService
    suspend fun searchFoods(query: String): Response<FoodResponse> {
        val apiKey = "fWwLFhFN3tz9uEfiPjWWPxPzS9OUSGHvWZqWWAs4"
        val response = apiService.searchFoods(query, apiKey)

        return response
    }
}