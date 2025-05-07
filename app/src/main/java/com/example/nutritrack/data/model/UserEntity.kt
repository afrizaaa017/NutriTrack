package com.example.nutritrack.data.model

import java.util.Date
import com.google.gson.annotations.SerializedName

data class User(
    val email: String,
    val password: String
)

data class UserProfile(

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("weight")
    val weight: Float,

    @SerializedName("height")
    val height: Float,

    @SerializedName("birthday")
    val birthday: String,

    @SerializedName("goal")
    val goal: String,

    @SerializedName("AMR")
    val amr: String,

    @SerializedName("calories_needed")
    val caloriesNeeded: Float,

    @SerializedName("gender")
    val gender: Boolean,

    @SerializedName("image")
    val image: String?
)

data class DailySummary(
    @SerializedName("email")
    val email: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("target_calories")
    val targetCalories: Int,

    @SerializedName("calories_consumed")
    val caloriesConsumed: Int,

    @SerializedName("fat_consumed")
    val fatConsumed: Int,

    @SerializedName("sugar_consumed")
    val sugarConsumed: Int,

    @SerializedName("carbs_consumed")
    val carbsConsumed: Int,

    @SerializedName("protein_consumed")
    val proteinConsumed: Int,

    @SerializedName("weight_recap")
    val weightRecap: Float,

    @SerializedName("height_recap")
    val heightRecap: Float
)