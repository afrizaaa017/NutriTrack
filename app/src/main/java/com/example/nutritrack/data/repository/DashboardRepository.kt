package com.example.nutritrack.data.repository

import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.DailySummary
import com.example.nutritrack.data.model.DailySummaryResponse
import com.example.nutritrack.data.model.ShowGraphResponse
import com.google.gson.reflect.TypeToken
import retrofit2.Response

class DashboardRepository {
    private val summaryApiService = RetrofitClient.instance
    suspend fun getDailyReport(authToken: String, date: String): DailySummaryResponse {
        return summaryApiService.getDailyReport("Bearer $authToken", date)
    }

    suspend fun showGraph(authToken: String, email: String, date: String): ShowGraphResponse {
        return summaryApiService.showGraph("Bearer $authToken", email, date)
    }
}