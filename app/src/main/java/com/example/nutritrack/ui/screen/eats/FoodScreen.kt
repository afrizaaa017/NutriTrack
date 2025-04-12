package com.example.nutritrack.ui.screen.eats

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import com.example.nutritrack.data.model.Food
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
//import com.android.volley.Response
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.Consume
import androidx.compose.material3.*
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
import com.example.nutritrack.data.model.RecommendedFood
import com.example.nutritrack.ui.auth.AuthViewModel
import com.example.nutritrack.ui.theme.BrokenWhite
import com.example.nutritrack.ui.theme.fontFamily

@Composable
fun FoodScreen(navController: NavController, mealType: String, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val repository = FoodRepository()
    val foodViewModel: FoodViewModel = viewModel(factory = FoodViewModelFactory(repository))
    val foods by foodViewModel.foodState.collectAsState()
    val foodMap by foodViewModel.foodMap.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: "gagal@gmail.com"
    val token = authViewModel.getToken()
    val summaryData by foodViewModel.summaryData.collectAsState()


    LaunchedEffect(token) {
        token?.let {
            foodViewModel.getFoodRecommendations(it, context)
            foodViewModel.getDailySummary(email)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(BorderStroke(1.dp, Color.Black)),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = summaryData.goals.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Goals", fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = summaryData.remaining.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Remaining", fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = summaryData.consumed.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Consume", fontSize = 10.sp)
                    }
                }
            }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.isNotBlank()) foodViewModel.searchFoods(it)
            },
            label = {Text(
                fontFamily = fontFamily,
                text = "Find Meals") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                cursorColor = GreenPrimary,
                focusedLabelColor = GreenPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isBlank()) {
            val recommendedFoods = foodMap[mealType] ?: emptyList()

            if (recommendedFoods.isNotEmpty()) {
                Text(
                    fontFamily = fontFamily,
                    text = "Recommended for ${mealType.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recommendedFoods) { food ->
                        FoodRecommendation(food = food, onAddClick = { selectedFood ->
                            val consumeData = foodViewModel.mapToConsumeData(email = email, mealType = mealType, portion = 1, recommendedFood = selectedFood)
                            foodViewModel.sendConsumeData(consumeData = consumeData, context = context)
                        })
                    }
                }
            } else {
                CircularProgressIndicator()
            }
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
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = food.description,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val caloriesText = food.foodNutrients
                            .firstOrNull { it.nutrientName == "Energy" }
                            ?.let { "${it.value} ${it.unitName}" } ?: "N/A"

                        Text(
                            text = "$caloriesText, ${food.householdServingFullText ?: "Unknown"} (${food.servingSize ?: "?"} ${food.servingSizeUnit ?: "?"})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

//                        Text(
//                            text = "Package Weight: ${food.packageWeight ?: "Unknown"}",
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Source: USDA",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
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
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Edit Portion Size",
                                fontWeight = FontWeight.Light,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Blue,
                                fontSize = 10.sp,
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
                                                            "Data sent successfully!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to send data: ${
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
                                                        "Error occurred: ${t.message}",
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
        FoodDetailDialog(food = food, nutrientMap = nutrientMap, onDismiss = { selectedFood = null }
        )
    }

    if (showPortionDialog) {
        AlertDialog(
            onDismissRequest = { showPortionDialog = false },
            title = { Text("Change Portion") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select portion size:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { if (portion > 1) portion-- }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease portion")
                        }
                        Text(text = portion.toString(), fontSize = 18.sp)
                        IconButton(onClick = { portion++ }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase portion")
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
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FoodDetailDialog(food: Food, nutrientMap: Map<String, String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
            ) {
                Text(
                    text = food.description,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
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
                    containerColor  = GreenPrimary
                )
            ) {
                Text("Close", color = Color.White)
            }
        },
        modifier = Modifier.heightIn(max = 430.dp)
    )
}

@Composable
fun FoodRecommendation(food: RecommendedFood, onAddClick: (RecommendedFood) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var showDetailDialog by remember { mutableStateOf(false) }
    var showPortionDialog by remember { mutableStateOf(false) }
    var portion by remember { mutableIntStateOf(1) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { showDetailDialog = true },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = food.foodName,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${food.totalCalories} kcal, ${food.portion} g",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Source: USDA",
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Portion: ${food.portion}",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Edit Portion Size",
                            fontFamily = fontFamily,
                            fontWeight = FontWeight.Light,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Blue,
                            modifier = Modifier.clickable { showPortionDialog = true }
                        )
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onAddClick(food)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(fontFamily = fontFamily, text = "Add Food", color = BrokenWhite)
                    }
                }
            }
        }
    }
    if (showDetailDialog) {
        RecommendedFoodDetailDialog(food = food, onDismiss = { showDetailDialog = false })
    }

    if (showPortionDialog) {
        AlertDialog(
            onDismissRequest = { showPortionDialog = false },
            title = { Text("Change Portion") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select portion size:", fontSize = 16.sp, fontFamily = fontFamily, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { if (portion > 1) portion-- }) {
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Decrease portion"
                            )
                        }
                        Text(text = portion.toString(), fontSize = 18.sp)
                        IconButton(onClick = { portion++ }) {
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                contentDescription = "Increase portion"
                            )
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
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecommendedFoodDetailDialog(
    food: RecommendedFood,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp)
            ) {
                Text(text = food.foodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                Text(fontFamily = fontFamily,text = "Portion: ${food.portion}g", modifier = Modifier.padding(8.dp))
                Text(fontFamily = fontFamily, text = "Calories: ${food.totalCalories}", modifier = Modifier.padding(8.dp))
                Text(fontFamily = fontFamily, text = "Carbohydrates: ${food.totalCarbs}g", modifier = Modifier.padding(8.dp))
                Text(fontFamily = fontFamily, text = "Protein: ${food.totalProtein}g", modifier = Modifier.padding(8.dp))
                Text(fontFamily = fontFamily, text = "Fat: ${food.totalFat}g", modifier = Modifier.padding(8.dp))
                Text(fontFamily = fontFamily, text = "Sugar: ${food.totalSugar}g", modifier = Modifier.padding(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor  = GreenPrimary)
            ) {
                Text("Close", color = Color.White)
            }
        },
        modifier = Modifier.heightIn(max = 430.dp)
    )
}