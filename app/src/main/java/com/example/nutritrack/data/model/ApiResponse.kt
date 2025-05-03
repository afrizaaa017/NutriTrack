package com.example.nutritrack.data.model

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    val totalHits: Int,
    val currentPage: Int,
    val totalPages: Int,
    @SerializedName("foods") val foods: List<Food>
)

data class SignUpResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class SignInResponse(
    @SerializedName("token") val token: String,
    @SerializedName("message") val message: String
)

data class SignOutResponse(
    @SerializedName("message") val message: String
)

data class ResetUpdateResponse(
    @SerializedName("message") val message: String
)

data class OnboardingResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UserProfile? = null
)

data class FoodRecommendationsResponse(
    val recommendations: Map<String, List<RecommendedFood>>
)

data class DailySummaryResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("data") val data: DailySummary? = null
)

data class ShowGraphResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<DailySummary>? = null,
    @SerializedName("count")
    val count: Int? = null
)