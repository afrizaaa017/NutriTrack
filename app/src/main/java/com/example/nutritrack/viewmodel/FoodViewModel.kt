package com.example.nutritrack.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.data.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _foodState = MutableStateFlow<List<Food>>(emptyList())
    val foodState: StateFlow<List<Food>> = _foodState

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
}