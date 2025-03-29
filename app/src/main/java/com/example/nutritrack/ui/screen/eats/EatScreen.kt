package com.example.nutritrack.ui.screen.eats


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutritrack.data.model.Food

@Composable
fun EatsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Meal Tracker", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        MealSection(title = "Breakfast", onAddClick = { navController.navigate("foodscreen/breakfast") })
        MealSection(title = "Lunch", onAddClick = { navController.navigate("foodscreen/lunch") })
        MealSection(title = "Dinner", onAddClick = { navController.navigate("foodscreen/dinner") })
        MealSection(title = "Snack", onAddClick = { navController.navigate("foodscreen/snack") })
    }
}

@Composable
fun MealSection(title: String, onAddClick: () -> Unit) {
    var foods by remember { mutableStateOf(listOf<Food>()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 20.sp)
                Button(onClick = onAddClick) {
                    Text(text = "Add Meal")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (foods.isNotEmpty()) {
                foods.forEach { food ->
                    FoodItem(food = food)
                }
            } else {
                Text(
                    text = "No meals added",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FoodItem(food: Food) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = food.description, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "Serving Size: ${food.servingSize} ${food.servingSizeUnit}", fontSize = 14.sp)
        }
    }
}