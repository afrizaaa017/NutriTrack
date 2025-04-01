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
    val birthday: Date,

    @SerializedName("goal")
    val goal: String,

    @SerializedName("AMR")
    val amr: String,

    @SerializedName("calories_needed")
    val caloriesNeeded: Float,

    @SerializedName("gender")
    val gender: Boolean,

    @SerializedName("image")
    val image: String?,

    @SerializedName("points")
    val points: Int = 0
)