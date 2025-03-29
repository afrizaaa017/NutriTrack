package com.example.nutritrack.data.model

import com.google.gson.annotations.SerializedName

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