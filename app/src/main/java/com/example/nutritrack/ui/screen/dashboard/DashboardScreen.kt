package com.example.nutritrack.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.data.repository.DashboardRepository
import com.example.nutritrack.ui.auth.AuthViewModel
import com.example.nutritrack.viewmodel.DashboardViewModel
import com.example.nutritrack.viewmodel.DashboardViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import java.util.*

@Composable
fun DashboardScreen(authViewModel: AuthViewModel) {
    val repository = remember { DashboardRepository() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository, firebaseAuth))
    val state by viewModel.dailyReportState.collectAsState()
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val coroutineScope = rememberCoroutineScope()
    var isLoadingToken by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Map ViewModel state to Composable parameters
    val summary = when (state) {
        is DashboardViewModel.DailyReportState.Success -> (state as DashboardViewModel.DailyReportState.Success).dailySummary
        else -> null
    }
    val isLoading = state is DashboardViewModel.DailyReportState.Loading || isLoadingToken
    val errorMessage = when (state) {
        is DashboardViewModel.DailyReportState.Error -> (state as DashboardViewModel.DailyReportState.Error).message
        else -> null
    }

    // Date formatter for display and API
    val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance()
    val isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color.White
                    ),
                    startY = 0f,
                    endY = 550f
                )
            )
            .padding(top = 16.dp)
    ) {
        // Date picker
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(8.dp, RoundedCornerShape(48.dp)),
            shape = RoundedCornerShape(48.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        selectedDate = Calendar.getInstance().apply {
                            time = selectedDate.time
                            add(Calendar.DAY_OF_MONTH, -1)
                        }
                    },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color(0xFF6B7280)
                    )
                }
                Row(
                    modifier = Modifier
                        .clickable {
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    selectedDate = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                    }
                                },
                                selectedDate.get(Calendar.YEAR),
                                selectedDate.get(Calendar.MONTH),
                                selectedDate.get(Calendar.DAY_OF_MONTH)
                            )
                            // Set max date to today
                            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
                            datePickerDialog.show()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = "Calendar",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isToday) "Today" else displayDateFormat.format(selectedDate.time),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1F2A44)
                    )
                }
                IconButton(
                    onClick = {
                        selectedDate = Calendar.getInstance().apply {
                            time = selectedDate.time
                            add(Calendar.DAY_OF_MONTH, 1)
                        }
                    },
                    enabled = !isLoading && !isToday
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        tint = Color(0xFF6B7280)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Background container for the main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Greeting
            Text(
                text = "Hey ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2A44)
            )
            Text(
                text = "Let's see your progress right now!",
                fontSize = 16.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFF10B981)
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (summary != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Calories Section with Circular Progress
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .padding(end = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "CALORIES",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { (summary.caloriesConsumed.toFloat() / summary.targetCalories.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                    modifier = Modifier.size(80.dp),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFFE5E7EB),
                                    strokeWidth = 8.dp
                                )
                                Text(
                                    text = "${summary.caloriesConsumed}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2A44)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.Center) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Goal : ${summary.targetCalories}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    // Macronutrients Section
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(210.dp)
                            .padding(start = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "MACRONUTRIENTS",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroCircle("Fat", summary.fatConsumed)
                                MacroCircle("Carbs", summary.carbsConsumed)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroCircle("Protein", summary.proteinConsumed)
                                MacroCircle("Sugar", summary.sugarConsumed)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Calories Section with Circular Progress
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .padding(end = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "CALORIES",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { 0F },
                                    modifier = Modifier.size(80.dp),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFFE5E7EB),
                                    strokeWidth = 8.dp
                                )
                                Text(
                                    text = "0",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2A44)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Consumed",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Macronutrients Section
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .padding(start = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "MACRONUTRIENTS",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroCircle("Fat", 0)
                                MacroCircle("Carbs", 0)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroCircle("Protein", 0)
                                MacroCircle("Sugar", 0)
                            }
                        }
                    }
                }
            }
        }
    }

    // Fetch data when date changes or on initial load
    LaunchedEffect(selectedDate.timeInMillis) {
        val authToken = authViewModel.getToken() ?: ""

        if (authToken.isNotEmpty()) {
            coroutineScope.launch {
                viewModel.fetchDailyReport(authToken, apiDateFormat.format(selectedDate.time))
            }
        } else {
            viewModel.setErrorState("User not authenticated. Please sign in again.")
        }
    }
}

@Composable
fun MacroCircle(macroName: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(Color(0xFFE5E7EB), RoundedCornerShape(50))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2A44)
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = macroName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        )
    }
}