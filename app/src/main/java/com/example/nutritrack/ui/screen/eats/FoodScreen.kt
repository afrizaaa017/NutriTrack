package com.example.nutritrack.ui.screen.eats

import android.health.connect.datatypes.MealType
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
import com.example.nutritrack.viewmodel.FoodViewModelFactory
import com.example.nutritrack.ui.theme.GreenPrimary

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
            FoodList(foods)
        }
    }
}

@Composable
fun FoodList(foods: List<Food>) {
    var selectedFood by remember { mutableStateOf<Food?>(null) }

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

                    val nutrientMap = mapOf("Energy" to "Calories")
                    val caloriesText = food.foodNutrients
                        .firstOrNull { it.nutrientName in nutrientMap.keys }
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
                }
            }
        }
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