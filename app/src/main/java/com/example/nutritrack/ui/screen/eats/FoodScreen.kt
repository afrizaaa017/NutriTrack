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
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.unit.sp
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
    var showPortionDialog by remember { mutableStateOf(false) }
    var portion by remember { mutableIntStateOf(1) }
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
        "Total Sugars" to "Total Sugars",
        "Carbohydrate, by difference" to  "Carbohydrate"

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column{
                            Text(
                                text = "Portion: $portion",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.body2,
                            )
                            Text(
                                text = "Edit Portion Size",
                                fontWeight = FontWeight.Light,
                                style = MaterialTheme.typography.body2,
                                color = Color.Blue,
                                modifier = Modifier.clickable { showPortionDialog = true }
                            )}

                            val nutrients = food.foodNutrients
                                .filter { it.nutrientName in nutrientMap.keys }
                                .associate { nutrientMap[it.nutrientName]!! to (it.value?.toFloat() ?: 0f) }

                            Button(
                                onClick = {
                                    selectedNutrients = nutrients
                                    showDialog = true
                                    coroutineScope.launch {
                                        val consumeData = Consume(
                                            email = email,
                                            foodId = food.fdcId,
                                            foodName = food.description,
                                            mealTime = mealType,
                                            portion = portion,
                                            totalSugar = nutrients["Total Sugars"] ?: 0f,
                                            totalCalories = nutrients["Energy"] ?: 0f,
                                            totalFat = nutrients["Fat"] ?: 0f,
                                            totalCarbs = nutrients["Carbohydrate"] ?: 0f,
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

    selectedFood?.let { food ->
        FoodDetailDialog(food = food, onDismiss = { selectedFood = null })
    }

    if (showPortionDialog) {
        AlertDialog(
            onDismissRequest = { showPortionDialog = false },
            title = { Text("Ubah Porsi") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pilih jumlah porsi:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { if (portion > 1) portion-- }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Kurangi porsi")
                        }
                        Text(text = portion.toString(), fontSize = 18.sp)
                        IconButton(onClick = { portion++ }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Tambah porsi")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showPortionDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showPortionDialog = false }) {
                    Text("Batal")
                }
            }
        )
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
                    "Total Sugars" to "Total Sugars",
                    "Carbohydrate, by difference" to "Carbohydrate"
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