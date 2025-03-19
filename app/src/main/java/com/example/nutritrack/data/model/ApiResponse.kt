package com.example.nutritrack.data.model

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    val totalHits: Int,
    val currentPage: Int,
    val totalPages: Int,
    @SerializedName("foods") val foods: List<Food>
)