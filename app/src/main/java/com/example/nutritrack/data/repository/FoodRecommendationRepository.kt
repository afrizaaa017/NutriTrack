package com.example.nutritrack.data.repository

import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.FoodRecommendationsResponse
import com.example.nutritrack.data.model.RecommendedFood
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FoodRecommendationRepository {
    fun fetchFoodRecommendations(
        token: String,
        onSuccess: (Map<String, List<RecommendedFood>>) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitClient.instance.getFoodRecommendations("Bearer $token")
            .enqueue(object : Callback<FoodRecommendationsResponse> {
                override fun onResponse(call: Call<FoodRecommendationsResponse>, response: Response<FoodRecommendationsResponse>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body()?.recommendations ?: emptyMap())
                    } else {
                        onError("Gagal mengambil data")
                    }
                }

                override fun onFailure(call: Call<FoodRecommendationsResponse>, t: Throwable) {
                    onError("Kesalahan jaringan")
                }
            })
    }
}
