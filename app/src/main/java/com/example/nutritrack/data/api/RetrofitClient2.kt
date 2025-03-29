package com.example.nutritrack.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.nutritrack.data.api.LaravelApiService

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.65:8000/api/"

    val instance: LaravelApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(LaravelApiService::class.java)
    }
}