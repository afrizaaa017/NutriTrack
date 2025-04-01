package com.example.nutritrack.ui.screen.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.viewmodel.DashboardViewModel
import com.example.nutritrack.data.model.RecommendedFood
import com.example.nutritrack.ui.auth.AuthViewModel

@Composable
fun DashboardScreen(authViewModel: AuthViewModel, foodRecommendationViewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current
    val token = authViewModel.getToken()
    val foodMap by foodRecommendationViewModel.foodMap.collectAsState()

    LaunchedEffect(token) {
        token?.let { foodRecommendationViewModel.loadFoodRecommendations(it, context) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Food recommendations for you",
                style = MaterialTheme.typography.h4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        val mealOrder = listOf("breakfast", "lunch", "dinner", "snack")
        mealOrder.forEach { mealTime ->
            val foods = foodMap[mealTime] ?: emptyList()

            if (foods.isNotEmpty()) {
                item { MealHeader(mealTime) }
                items(foods) { food -> FoodItem(food) }
            }
        }
    }
}

@Composable
fun MealHeader(mealTime: String) {
    Text(
        text = mealTime.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.h5,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun FoodItem(food: RecommendedFood) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = food.foodName, style = MaterialTheme.typography.h6)
            Text(text = "Porsi: ${food.portion}g")
            Text(text = "Kalori: ${food.totalCalories}")
            Text(text = "Karbohidrat: ${food.totalCarbs}g")
            Text(text = "Protein: ${food.totalProtein}g")
            Text(text = "Lemak: ${food.totalFat}g")
            Text(text = "Gula: ${food.totalSugar}g")
        }
    }
}