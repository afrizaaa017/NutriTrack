package com.example.nutritrack.data.api

import com.example.nutritrack.data.model.OnboardingResponse
import com.example.nutritrack.data.model.SignInResponse
import com.example.nutritrack.data.model.SignOutResponse
import com.example.nutritrack.data.model.SignUpResponse
import com.example.nutritrack.data.model.User
import com.example.nutritrack.data.model.UserProfile
import com.example.nutritrack.data.model.resetUpdateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface LaravelApiService {
    @POST("signup")
    fun signUp(@Body request: User): Call<SignUpResponse>

    @POST("signin")
    fun signIn(@Body request: User): Call<SignInResponse>

    @DELETE("signout")
    fun signOut(@Header("Authorization") token: String): Call<SignOutResponse>

    @POST("check-and-update-password")
    fun checkAndUpdatePassword(@Body request: User): Call<resetUpdateResponse>

    @POST("onboarding")
    fun completeOnboarding(
        @Header("Authorization") authToken: String,
        @Body profile: UserProfile
    ): Call<OnboardingResponse>
}
