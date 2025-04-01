package com.example.nutritrack.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.nutritrack.data.model.RecommendedFood
import com.example.nutritrack.data.repository.FoodRecommendationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {
    private val _foodMap = MutableStateFlow<Map<String, List<RecommendedFood>>>(emptyMap())
    val foodMap: StateFlow<Map<String, List<RecommendedFood>>> = _foodMap.asStateFlow()

    fun loadFoodRecommendations(token: String, context: Context) {
        FoodRecommendationRepository.fetchFoodRecommendations(
            token,
            onSuccess = { recommendations -> _foodMap.value = recommendations },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        )
    }
}