package com.example.nutritrack.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.Response
import com.example.nutritrack.data.model.FoodResponse
import com.example.nutritrack.data.model.Consume
import com.example.nutritrack.data.model.OnboardingResponse
import com.example.nutritrack.data.model.SignInResponse
import com.example.nutritrack.data.model.SignOutResponse
import com.example.nutritrack.data.model.SignUpResponse
import com.example.nutritrack.data.model.User
import com.example.nutritrack.data.model.UserProfile
import com.example.nutritrack.data.model.ResetUpdateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Header("X-Api-Key") apiKey: String
    ): Response<FoodResponse>
}

interface LaravelApiService {
    @POST("consume")
    fun createConsume(@Body consume: Consume): Call<Consume>

    @POST("signup")
    fun signUp(@Body request: User): Call<SignUpResponse>

    @POST("signin")
    fun signIn(@Body request: User): Call<SignInResponse>

    @DELETE("signout")
    fun signOut(@Header("Authorization") token: String): Call<SignOutResponse>

    @POST("check-and-update-password")
    fun checkAndUpdatePassword(@Body request: User): Call<ResetUpdateResponse>

    @POST("onboarding")
    fun completeOnboarding(
        @Header("Authorization") authToken: String,
        @Body profile: UserProfile
    ): Call<OnboardingResponse>
}
