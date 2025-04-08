package com.example.nutritrack.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.data.model.Food
import com.example.nutritrack.data.model.RecommendedFood
import com.example.nutritrack.data.model.Consume
import com.example.nutritrack.data.repository.FoodRecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _foodState = MutableStateFlow<List<Food>>(emptyList())
    val foodState: StateFlow<List<Food>> = _foodState

    private val _foodMap = MutableStateFlow<Map<String, List<RecommendedFood>>>(emptyMap())
    val foodMap: StateFlow<Map<String, List<RecommendedFood>>> = _foodMap

    fun searchFoods(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchFoods(query)
                Log.d("FoodViewModel", "API Response: $response")

                if (response.isSuccessful) {
                    val foodList = response.body()?.foods ?: emptyList()
                    Log.d("FoodViewModel", "Parsed Food List: $foodList")
                    _foodState.value = foodList
                } else {
                    Log.e("FoodViewModel", "API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FoodViewModel", "Exception: ${e.message}")
                Log.e("FoodViewModel", "Error fetching foods", e)
            }
        }
    }

    fun getFoodRecommendations(token: String, context: Context) {
        FoodRecommendationRepository.fetchFoodRecommendations(
            token,
            onSuccess = { recommendations -> _foodMap.value = recommendations },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        )
    }

    fun mapToConsumeData(email: String, mealType: String, portion: Int, recommendedFood: RecommendedFood): Consume {
        return Consume(
            email = email,
            foodId = recommendedFood.foodId,
            foodName = recommendedFood.foodName,
            mealTime = mealType,
            portion = portion,
            totalSugar = recommendedFood.totalSugar ,
            totalCalories = recommendedFood.totalCalories ,
            totalFat = recommendedFood.totalFat ,
            totalCarbs = recommendedFood.totalCarbs ,
            totalProtein = recommendedFood.totalProtein
        )
    }

    fun sendConsumeData(consumeData: Consume, context: Context) {
        RetrofitClient.instance.createConsume(consumeData)
            .enqueue(object : Callback<Consume> {
                override fun onResponse(call: Call<Consume>, response: Response<Consume>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Data sent successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to send data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Consume>, t: Throwable) {
                    Toast.makeText(context, "Error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}