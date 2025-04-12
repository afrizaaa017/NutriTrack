package com.example.nutritrack.data.model

import com.google.gson.annotations.SerializedName

data class Food(
    val fdcId: Int,
    val dataType: String,
    val description: String,
    val foodNutrients: List<FoodNutrient>,
    val ingredients: String?,
    val score: Double,
    val packageWeight: String?,
    val servingSize: String?,
    val servingSizeUnit: String?,
    val householdServingFullText: String?,
)

data class FoodNutrient(
    val nutrientId: Int,
    val nutrientName: String?,
    val nutrientNumber: String?,
    val unitName: String,
    val derivationCode: String?,
    val derivationDescription: String,
    val value: Double,
)

data class Consume(
    @SerializedName("email") val email: String,
    @SerializedName("food_id") val foodId: Int,
    @SerializedName("meal_time") val mealTime: String,
    @SerializedName("portion") val portion: Int,
    @SerializedName("total_sugar") val totalSugar: Float,
    @SerializedName("total_calories") val totalCalories: Float,
    @SerializedName("total_fat") val totalFat: Float,
    @SerializedName("total_carbs") val totalCarbs: Float,
    @SerializedName("total_protein") val totalProtein: Float,
    @SerializedName("food_name") val foodName: String
)

data class RecommendedFood(
    @SerializedName("food_id") val foodId: Int,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("portion") val portion: Int,
    @SerializedName("total_sugar") val totalSugar: Float,
    @SerializedName("total_calories") val totalCalories: Float,
    @SerializedName("total_fat") val totalFat: Float,
    @SerializedName("total_carbs") val totalCarbs: Float,
    @SerializedName("total_protein") val totalProtein: Float
)

data class SummaryData(
    @SerializedName("calories_needed") val goals: Int,
    @SerializedName("total_consumed") val consumed: Int,
    @SerializedName("total_remaining") val remaining: Int
)