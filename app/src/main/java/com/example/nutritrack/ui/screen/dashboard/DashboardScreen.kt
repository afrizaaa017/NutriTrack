package com.example.nutritrack.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import com.example.nutritrack.data.model.DailySummary
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect

@Composable
fun DashboardScreen(authViewModel: AuthViewModel) {
    val repository = remember { DashboardRepository() }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }

    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository, firebaseAuth))
    val state by viewModel.dailyReportState.collectAsState()
    val graphState by viewModel.graphState.collectAsState()
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedGraphType by remember { mutableStateOf("weight") }
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

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour in 0..11 -> "Good Morning!"
        currentHour in 12..16 -> "Good Afternoon!"
        else -> "Good Evening!"
    }

    // Fetch graph data saat layar dimuat
    LaunchedEffect(Unit) {
        val authToken = authViewModel.getToken() ?: ""
        if (authToken.isNotEmpty()) {
            coroutineScope.launch {
                viewModel.fetchGraphData(authToken)
            }
        } else {
            viewModel.setErrorState("User not authenticated. Please sign in again.")
        }
    }

    LazyColumn(
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
        item {
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
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            // Background container for the main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Greeting
                Text(
                    text = greeting,
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
                                        color = MaterialTheme.colorScheme.secondary,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card untuk grafik berat badan dan tinggi badan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Tombol untuk memilih jenis grafik
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Button(
                                    onClick = { selectedGraphType = "weight" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedGraphType == "weight") MaterialTheme.colorScheme.primary else Color.White,
                                        contentColor = if (selectedGraphType == "weight") Color.White else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(end = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 0.dp)
                                ) {
                                    Text("Weight", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = { selectedGraphType = "height" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedGraphType == "height") MaterialTheme.colorScheme.primary else Color.White,
                                        contentColor = if (selectedGraphType == "height") Color.White else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(end = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 0.dp)
                                ) {
                                    Text("Height", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Grafik menggunakan Canvas
                            when (graphState) {
                                is DashboardViewModel.GraphState.Initial,
                                is DashboardViewModel.GraphState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.CenterHorizontally),
                                        color = Color(0xFF10B981)
                                    )
                                }
                                is DashboardViewModel.GraphState.Success -> {
                                    val data = (graphState as DashboardViewModel.GraphState.Success).data.reversed()
                                    if (data.isNotEmpty()) {
                                        // Debug data untuk memastikan nilai yang diterima
                                        data.forEach { entry ->
                                            println("Date: ${entry.date}, Weight: ${entry.weightRecap}, Height: ${entry.heightRecap}")
                                        }
                                        GraphCanvas(
                                            data = data,
                                            isWeightGraph = selectedGraphType == "weight"
                                        )
                                    } else {
                                        Text(
                                            text = "No data available",
                                            color = Color(0xFF6B7280),
                                            fontSize = 14.sp,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                                is DashboardViewModel.GraphState.Empty -> {
                                    Text(
                                        text = "No recap available",
                                        color = Color(0xFF6B7280),
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                                is DashboardViewModel.GraphState.Error -> {
                                    Text(
                                        text = (graphState as DashboardViewModel.GraphState.Error).message,
                                        color = Color(0xFFEF4444),
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card baru untuk update weight/height
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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
                                text = "Does your weight or height has any changes?",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Logika untuk update weight/height (misalnya navigasi ke halaman update)
                                    // Tambahkan logika sesuai kebutuhan, misalnya:
                                    // navController.navigate("updateWeightHeight")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .padding(end = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 12.dp,
                                    disabledElevation = 0.dp)
                            ) {
                                Text("Update Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                                        color = MaterialTheme.colorScheme.secondary,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card untuk grafik berat badan dan tinggi badan (ketika summary null)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Tombol untuk memilih jenis grafik
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Button(
                                    onClick = { selectedGraphType = "weight" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedGraphType == "weight") MaterialTheme.colorScheme.primary else Color.White,
                                        contentColor = if (selectedGraphType == "weight") Color.White else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(end = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 0.dp)
                                ) {
                                    Text("Weight", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = { selectedGraphType = "height" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedGraphType == "height") MaterialTheme.colorScheme.primary else Color.White,
                                        contentColor = if (selectedGraphType == "height") Color.White else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .padding(end = 8.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 0.dp)
                                ) {
                                    Text("Height", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // Grafik menggunakan Canvas
                            when (graphState) {
                                is DashboardViewModel.GraphState.Initial,
                                is DashboardViewModel.GraphState.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.CenterHorizontally),
                                        color = Color(0xFF10B981)
                                    )
                                }
                                is DashboardViewModel.GraphState.Success -> {
                                    val data = (graphState as DashboardViewModel.GraphState.Success).data.reversed()
                                    if (data.isNotEmpty()) {
                                        // Debug data untuk memastikan nilai yang diterima
                                        data.forEach { entry ->
                                            println("Date: ${entry.date}, Weight: ${entry.weightRecap}, Height: ${entry.heightRecap}")
                                        }
                                        GraphCanvas(
                                            data = data,
                                            isWeightGraph = selectedGraphType == "weight"
                                        )
                                    } else {
                                        Text(
                                            text = "No data available",
                                            color = Color(0xFF6B7280),
                                            fontSize = 14.sp,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                                is DashboardViewModel.GraphState.Empty -> {
                                    Text(
                                        text = "No recap available",
                                        color = Color(0xFF6B7280),
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                                is DashboardViewModel.GraphState.Error -> {
                                    Text(
                                        text = (graphState as DashboardViewModel.GraphState.Error).message,
                                        color = Color(0xFFEF4444),
                                        fontSize = 14.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card baru untuk update weight/height
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
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
                                text = "Does your weight or height has any changes?",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Logika untuk update weight/height (misalnya navigasi ke halaman update)
                                    // Tambahkan logika sesuai kebutuhan, misalnya:
                                    // navController.navigate("updateWeightHeight")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .padding(end = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 12.dp,
                                    disabledElevation = 0.dp)
                            ) {
                                Text("Update Now", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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

@Composable
fun GraphCanvas(data: List<DailySummary>, isWeightGraph: Boolean) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = Color(0xFFE5E7EB)
    val textColor = Color(0xFF6B7280)
    val fillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) // Warna tertiary dengan opasitas rendah
    val dashedLineColor = textColor.copy(alpha = 0.8f) // Warna garis putus-putus dengan opasitas rendah

    // State untuk menyimpan informasi titik yang diklik
    var selectedPoint by remember { mutableStateOf<Pair<Offset, Float>?>(null) }

    // Hitung nilai maksimum dan minimum untuk sumbu Y
    val values = if (isWeightGraph) {
        data.map { it.weightRecap }
    } else {
        data.map { it.heightRecap }
    }
    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(if (isWeightGraph) 100f else 200f)
    val minValue = (values.minOrNull() ?: 0f).coerceAtMost(if (isWeightGraph) 0f else 100f)
    val valueRange = if (maxValue == minValue) {
        if (maxValue == 0f) 100f else maxValue * 0.2f // Pastikan valueRange tidak nol
    } else {
        maxValue - minValue
    }

    // Format tanggal untuk sumbu X
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dates = data.map { dateFormat.format(SimpleDateFormat("yyyy-MM-dd").parse(it.date) ?: Date()) }

    // Simpan posisi titik untuk deteksi sentuhan
    val points = mutableListOf<Offset>()
    val pointValues = mutableListOf<Float>()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(end = 16.dp, bottom = 30.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Deteksi titik terdekat yang diklik dengan area sensitivitas lebih besar
                    var closestPointIndex = -1
                    var minDistance = Float.MAX_VALUE
                    points.forEachIndexed { index, point ->
                        val distance = kotlin.math.sqrt(
                            (offset.x - point.x).pow(2) + (offset.y - point.y).pow(2)
                        )
                        if (distance < minDistance && distance < 50f) {
                            minDistance = distance
                            closestPointIndex = index
                        }
                    }
                    selectedPoint = if (closestPointIndex != -1) {
                        Pair(points[closestPointIndex], pointValues[closestPointIndex])
                    } else {
                        null
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val pointCount = data.size

        // Reset daftar titik
        points.clear()
        pointValues.clear()

        // Gambar kotak berwarna untuk ruang genap
        val gridLines = 5
        for (i in 0 until gridLines step 2) {
            val topY = canvasHeight * (1f - (i + 1).toFloat() / gridLines)
            val bottomY = canvasHeight * (1f - i.toFloat() / gridLines)
            drawRect(
                color = fillColor,
                topLeft = Offset(0f, topY),
                size = Size(canvasWidth, bottomY - topY)
            )
        }

        // Gambar garis grid horizontal
        for (i in 0..gridLines) {
            val y = canvasHeight * (1f - i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f
            )
        }

        // Gambar garis data
        if (pointCount > 0) {
            val path = Path()
            for (i in 0 until pointCount) {
                val x = canvasWidth * i / (pointCount - 1).coerceAtLeast(1)
                val value = values[i]
                val normalizedValue = if (valueRange == 0f) 0f else (value - minValue) / valueRange
                val y = canvasHeight * (1f - normalizedValue)
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                // Simpan posisi titik dan nilai
                points.add(Offset(x, y))
                pointValues.add(value)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 6f) // Garis lebih tebal
            )

            // Gambar titik data dan garis putus-putus ke sumbu X
            for (i in 0 until pointCount) {
                val x = canvasWidth * i / (pointCount - 1).coerceAtLeast(1)
                val value = values[i]
                val normalizedValue = if (valueRange == 0f) 0f else (value - minValue) / valueRange
                val y = canvasHeight * (1f - normalizedValue)

                // Gambar garis putus-putus dari titik ke sumbu X
                drawLine(
                    color = dashedLineColor,
                    start = Offset(x, y),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Pola putus-putus
                )

                // Gambar titik data
                drawCircle(
                    color = lineColor,
                    radius = 8f, // Titik lebih besar
                    center = Offset(x, y)
                )

                // Tampilkan nilai di atas titik yang dipilih dengan perbandingan jarak hanya pada sumbu X
                if (selectedPoint != null) {
                    val xDistance = kotlin.math.abs(x - selectedPoint!!.first.x)
                    if (xDistance < 10f) { // Hanya bandingkan koordinat X dengan toleransi
                        val displayValue = value.toInt() // Konversi ke integer
                        drawContext.canvas.nativeCanvas.drawText(
                            "$displayValue ${if (isWeightGraph) "kg" else "cm"}",
                            x,
                            y - 15.dp.toPx(), // Posisi teks di atas titik
                            android.graphics.Paint().apply {
                                color = textColor.toArgb()
                                textSize = 12.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                        )
                    }
                }
            }
        }

        // Gambar label sumbu X
        dates.forEachIndexed { index, date ->
            val x = canvasWidth * index / (pointCount - 1).coerceAtLeast(1)
            drawContext.canvas.nativeCanvas.drawText(
                date,
                x,
                canvasHeight + 30.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}