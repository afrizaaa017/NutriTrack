package com.example.nutritrack.data.repository

import android.util.Log
import retrofit2.Response
import com.example.nutritrack.data.api.ApiClient
import com.example.nutritrack.data.model.FoodResponse
import com.example.nutritrack.BuildConfig
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.SummaryData

class FoodRepository  {
    private val apiService = ApiClient.apiService
    suspend fun searchFoods(query: String): Response<FoodResponse> {
        val apiKey = BuildConfig.PLACES_API_KEY
        val response = apiService.searchFoods(query, apiKey)

        return response
    }

    private val summaryApiService = RetrofitClient.instance
    suspend fun getDailySummary(email: String): SummaryData {
        return summaryApiService.getDailySummary(email)
    }

}