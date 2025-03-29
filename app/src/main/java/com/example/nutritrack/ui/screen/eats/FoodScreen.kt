package com.example.nutritrack.ui.screen.eats

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.data.repository.FoodRepository
import com.example.nutritrack.viewmodel.FoodViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import com.example.nutritrack.data.model.Food
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
//import com.android.volley.Response
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.Consume
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
import com.example.nutritrack.viewmodel.FoodViewModelFactory
import com.example.nutritrack.ui.theme.GreenPrimary
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import android.health.connect.datatypes.MealType
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController


@Composable
fun FoodScreen(navController: NavController, mealType: String) {
    val repository = FoodRepository()
    val viewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(repository))
    val foods by viewModel.foodState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        var searchQuery by remember { mutableStateOf("") }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.isNotBlank()) viewModel.searchFoods(it)
            },
            label = { Text("Find Meals") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = GreenPrimary,
                cursorColor = GreenPrimary,
                focusedLabelColor = GreenPrimary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
        if (foods.isEmpty()) {
            CircularProgressIndicator()
        } else {
            FoodList(mealType, foods)
        }
    }
}

@Composable
fun FoodList(
    mealType: String,
    foods: List<Food>
    ) {
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedNutrients by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "gagal@gmail.com"

    val nutrientMap = mapOf(
        "Energy" to "Energy",
        "Fiber, total dietary" to "Fiber",
        "Sodium, Na" to "Sodium",
        "Total lipid (fat)" to "Fat",
        "Protein" to "Protein",
        "Total Sugars" to "Total Sugars"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(foods) { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { selectedFood = food },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = food.description,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(4.dp))

//                        val nutrientMap = mapOf("Energy" to "Calories")
                        val caloriesText = food.foodNutrients
                            .firstOrNull { it.nutrientName == "Energy" }
                            ?.let { "${it.value} ${it.unitName}" } ?: "N/A"

                        Text(
                            text = "$caloriesText, ${food.householdServingFullText ?: "Unknown"} (${food.servingSize ?: "?"} ${food.servingSizeUnit ?: "?"})",
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Package Weight: ${food.packageWeight ?: "Unknown"}",
                            style = MaterialTheme.typography.body2
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Source: USDA",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body2
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row untuk mengatur posisi teks dan button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add to consumption",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.body2
                            )



                            // Konversi daftar foodNutrients menjadi map
                            val nutrients = food.foodNutrients
                                .filter { it.nutrientName in nutrientMap.keys }
                                .associate { nutrientMap[it.nutrientName]!! to (it.value?.toFloat() ?: 0f) }


                            Button(
                                onClick = {
                                    selectedNutrients = nutrients // Simpan data makanan yang dipilih
                                    showDialog = true
                                    coroutineScope.launch {
                                        val consumeData = Consume(
                                            email = email.toString(),
                                            foodId = 1,
                                            mealTime = mealType,
                                            portion = 1,
                                            totalSugar = nutrients["Total Sugars"] ?: 0f,
                                            totalCalories = nutrients["Energy"] ?: 0f,
                                            totalFat = nutrients["Fat"] ?: 0f,
                                            totalCarbs = 1f,
                                            totalProtein = nutrients["Protein"] ?: 0f
                                        )

                                        RetrofitClient.instance.createConsume(consumeData)
                                            .enqueue(object : Callback<Consume> {
                                                override fun onResponse(
                                                    call: Call<Consume>,
                                                    response: Response<Consume>
                                                ) {
                                                    if (response.isSuccessful) {
                                                        Toast.makeText(
                                                            context,
                                                            "Data berhasil dikirim!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Gagal mengirim data: ${
                                                                response.errorBody()?.string()
                                                            }",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<Consume>,
                                                    t: Throwable
                                                ) {
                                                    Toast.makeText(
                                                        context,
                                                        "Terjadi kesalahan: ${t.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                    }
                                }
                            ) {
                                Text("Add Food")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi") },
            text = { Text("Apakah kamu yakin ingin menambahkan makanan ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        coroutineScope.launch {
                            val consumeData = Consume(
                                email = email,
                                foodId = 1,
                                mealTime = mealType,
                                portion = 1,
                                totalSugar = selectedNutrients["Total Sugars"] ?: 0f,
                                totalCalories = selectedNutrients["Energy"] ?: 0f,
                                totalFat = selectedNutrients["Fat"] ?: 0f,
                                totalCarbs = 1f,
                                totalProtein = selectedNutrients["Protein"] ?: 0f
                            )

                            RetrofitClient.instance.createConsume(consumeData)
                                .enqueue(object : Callback<Consume> {
                                    override fun onResponse(
                                        call: Call<Consume>,
                                        response: Response<Consume>
                                    ) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Data berhasil dikirim!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Gagal mengirim data: ${
                                                    response.errorBody()?.string()
                                                }",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<Consume>,
                                        t: Throwable
                                    ) {
                                        Toast.makeText(
                                            context,
                                            "Terjadi kesalahan: ${t.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                        }
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    selectedFood?.let { food ->
        FoodDetailDialog(food = food, onDismiss = { selectedFood = null })
    }
}

@Composable
fun FoodDetailDialog(food: Food, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = food.description, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                Text(text = "Brand: ${food.brandOwner ?: "Unknown"}")
                Text(text = "Scientific Name: ${food.scientificName ?: "N/A"}")
                Text(text = "Food Code: ${food.foodCode}")
                Text(text = "Publication Date: ${food.publicationDate}")
                Text(text = "Ingredients: ${food.ingredients ?: "N/A"}")
                Text(text = "Score: ${food.score}")

                Spacer(modifier = Modifier.height(8.dp))

                val nutrientMap = mapOf(
                    "Energy" to "Energy",
                    "Fiber, total dietary" to "Fiber",
                    "Sodium, Na" to "Sodium",
                    "Total lipid (fat)" to "Fat",
                    "Protein" to "Protein",
                    "Total Sugars" to "Total Sugars"
                )

                food.foodNutrients
                    .filter { nutrient -> nutrient.nutrientName in nutrientMap.keys }
                    .forEach { nutrient ->
                        val displayName = nutrientMap[nutrient.nutrientName] ?: nutrient.nutrientName
                        Text(text = "$displayName: ${nutrient.value} ${nutrient.unitName}", modifier = Modifier.padding(8.dp))
                    }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor  = GreenPrimary
                )
            ) {
                Text("Close", color = Color.White)
            }
        }

    )
}