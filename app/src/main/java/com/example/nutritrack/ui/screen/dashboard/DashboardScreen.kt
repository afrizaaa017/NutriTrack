package com.example.nutritrack.ui.screen.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.viewmodel.DashboardViewModel
import com.example.nutritrack.data.model.RecommendedFood
import com.example.nutritrack.ui.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.nutritrack.data.model.Consume
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.ui.theme.BrokenWhite
import com.example.nutritrack.ui.theme.GreenPrimary
import com.example.nutritrack.ui.theme.PurpleGrey40
import com.example.nutritrack.ui.theme.fontFamily
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

@Composable
fun DashboardScreen(authViewModel: AuthViewModel, foodRecommendationViewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current
    val token = authViewModel.getToken()
    val foodMap by foodRecommendationViewModel.foodMap.collectAsState()
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "gagal@gmail.com"

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
                fontFamily = fontFamily,
                text = "Food recommendations for you",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Left
            )
        }

        val mealOrder = listOf("breakfast", "lunch", "dinner", "snack")
        mealOrder.forEach { mealTime ->
            val foods = foodMap[mealTime] ?: emptyList()

            if (foods.isNotEmpty()) {
                item { MealHeader(mealTime) }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(foods) { food ->
                            FoodItem(food = food, onAddClick = { selectedFood ->
                                val consumeData = Consume(
                                    email = email,
                                    foodId = selectedFood.foodId,
                                    foodName = selectedFood.foodName,
                                    mealTime = mealTime,
                                    portion = selectedFood.portion,
                                    totalSugar = selectedFood.totalSugar,
                                    totalCalories = selectedFood.totalCalories,
                                    totalFat = selectedFood.totalFat,
                                    totalCarbs = selectedFood.totalCarbs,
                                    totalProtein = selectedFood.totalProtein
                                )

                                RetrofitClient.instance.createConsume(consumeData)
                                    .enqueue(object : Callback<Consume> {
                                        override fun onResponse(call: Call<Consume>, response: Response<Consume>) {
                                            if (response.isSuccessful) {
                                                Log.d("AddFood", "Sukses mengirim data: ${response.body()}")
                                                Toast.makeText(context, "Data berhasil dikirim!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Log.e("AddFood", "Gagal mengirim data, status: ${response.code()}, error: ${response.errorBody()?.string()}")
                                                Toast.makeText(context, "Gagal mengirim data", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<Consume>, t: Throwable) {
                                            Log.e("AddFood", "Kesalahan jaringan: ${t.message}")
                                            Toast.makeText(context, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            })
                        }
                    }
                }
                if (mealTime != mealOrder.last()) {
                    item {
                        Divider(
                            color = PurpleGrey40,
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealHeader(mealTime: String) {
    Text(
        fontFamily = fontFamily,
        text = mealTime.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.h5,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Start
    )
}

@Composable
fun FoodItem(food: RecommendedFood, onAddClick: (RecommendedFood) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .width(250.dp)
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(fontFamily = fontFamily, text = food.foodName, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            Text(fontFamily = fontFamily, text = "Porsi: ${food.portion}g")
            Text(fontFamily = fontFamily, text = "Kalori: ${food.totalCalories}")
            Text(fontFamily = fontFamily, text = "Karbohidrat: ${food.totalCarbs}g")
            Text(fontFamily = fontFamily, text = "Protein: ${food.totalProtein}g")
            Text(fontFamily = fontFamily, text = "Lemak: ${food.totalFat}g")
            Text(fontFamily = fontFamily, text = "Gula: ${food.totalSugar}g")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        onAddClick(food)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(fontFamily = fontFamily, text = "Add Food", color = BrokenWhite)
            }
        }
    }
}