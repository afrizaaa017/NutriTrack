package com.example.nutritrack.ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Data Model
data class Food(val id: Int, val name: String, val calories: Int)

// Retrofit API Interface
interface ApiService {
    @GET("foods") // Sesuaikan dengan endpoint API Laravel
    suspend fun getFoods(): List<Food>
}

// ViewModel untuk Dashboard
class DashboardViewModel : ViewModel() {
    private val api = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8000/api/") // Sesuaikan dengan IP Emulator atau perangkat
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    private val _foods = MutableLiveData<List<Food>>()
    val foods: LiveData<List<Food>> = _foods

    init {
        fetchFoods()
    }

    private fun fetchFoods() {
        viewModelScope.launch {
            try {
                _foods.value = api.getFoods()
            } catch (e: Exception) {
                _foods.value = emptyList() // Jika gagal, tampilkan list kosong
            }
        }
    }
}

// Tampilan Dashboard
@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = viewModel()
    val foods by viewModel.foods.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Daftar Makanan", style = MaterialTheme.typography.headlineSmall)

        if (foods.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn {
                items(foods) { food ->
                    FoodItem(food)
                }
            }
        }
    }
}

// Tampilan Item Makanan
@Composable
fun FoodItem(food: Food) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Gunakan ini jika pakai Material 3
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Kalori: ${food.calories} kcal", style = MaterialTheme.typography.bodySmall)
        }
    }
}
