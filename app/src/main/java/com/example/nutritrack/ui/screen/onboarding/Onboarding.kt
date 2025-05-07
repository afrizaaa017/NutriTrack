package com.example.nutritrack.ui.screen.onboarding

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.MainScreen
import com.example.nutritrack.viewmodel.AuthViewModel
import com.example.nutritrack.viewmodel.AuthState
import com.example.nutritrack.ui.theme.BrokenWhite
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import java.util.*

@Composable
fun OnboardingScreen(navController: NavController, authViewModel: AuthViewModel) {
    var currentStep by rememberSaveable { mutableStateOf(1) }

    var lastName by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }

    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    var selectedActivity by rememberSaveable { mutableStateOf("") }
    var selectedGoal by rememberSaveable { mutableStateOf("") }

    var isButtonPressed by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.maxDate = Calendar.getInstance().timeInMillis // Restrict to today
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { currentStep / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    1 -> {
                        Text(
                            text = "Welcome Aboard!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Let's personalize your journey with a few quick steps!",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                isButtonPressed = true
                                isButtonPressed = false
                                currentStep = 2
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color(0xFFECEFF1)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp,
                                disabledElevation = 2.dp
                            )
                        ) {
                            Text("Get Started!", fontSize = 18.sp)
                        }
                    }

                    2 -> {
                        Text(
                            text = "Personal Information",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Step 1 of 4",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(280.dp),
                            singleLine = true,
                            isError = isButtonPressed && firstName.isEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                        if (isButtonPressed && firstName.isEmpty()) {
                            Text(
                                "First Name cannot be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(280.dp),
                            singleLine = true,
                            isError = isButtonPressed && lastName.isEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                        if (isButtonPressed && lastName.isEmpty()) {
                            Text(
                                "Last Name cannot be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = {},
                            label = { Text("Date of Birth") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            isError = isButtonPressed && birthDate.isEmpty(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Pick Date",
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                )
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .clickable { datePickerDialog.show() },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                        if (isButtonPressed && birthDate.isEmpty()) {
                            Text(
                                "Date of Birth cannot be empty",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Gender",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("male", "female").forEach { gender ->
                                Card(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .clickable { selectedGender = gender }
                                        .border(
                                            width = if (selectedGender == gender) 2.dp else 1.dp,
                                            color = if (selectedGender == gender) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedGender == gender) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(gender, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        if (isButtonPressed && selectedGender.isEmpty()) {
                            Text(
                                "Please select a gender",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                isButtonPressed = true
                                if (firstName.isNotEmpty() && lastName.isNotEmpty() && birthDate.isNotEmpty() && selectedGender.isNotEmpty()) {
                                    isButtonPressed = false
                                    currentStep = 3
                                }
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color(0xFFECEFF1)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 12.dp,
                                disabledElevation = 2.dp
                            )
                        ) {
                            Text("Next", fontSize = 18.sp)
                        }
                    }

                    3 -> {
                        Text(
                            text = "Body Measurements",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Step 2 of 4",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height (cm)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                isError = isButtonPressed && height.isEmpty(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                                )
                            )
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                isError = isButtonPressed && weight.isEmpty(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }
                        if (isButtonPressed && (height.isEmpty() || weight.isEmpty())) {
                            Text(
                                "Please fill in both height and weight",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(24.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp,
                                    disabledElevation = 2.dp
                                )
                            ) {
                                Text("Back", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    isButtonPressed = true
                                    if (height.isNotEmpty() && weight.isNotEmpty()) {
                                        isButtonPressed = false
                                        currentStep = 4
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = Color(0xFFECEFF1)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 12.dp,
                                    disabledElevation = 2.dp
                                )
                            ) {
                                Text("Next", fontSize = 18.sp)
                            }
                        }
                    }

                    4 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Activity Level",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Step 3 of 4",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val activityLevels = listOf(
                                "Sedentary active" to "Minimal exercise, mostly sitting (e.g., office work)",
                                "Lightly active" to "Light exercise/sports 1-3 days/week",
                                "Moderately active" to "Moderate exercise/sports 3-5 days/week",
                                "Highly active" to "Hard exercise/sports 6-7 days/week",
                                "Extremely active" to "Very intense exercise, physical job, or training"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                activityLevels.forEach { (level, description) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedActivity = level }
                                            .border(
                                                width = if (selectedActivity == level) 2.dp else 1.dp,
                                                color = if (selectedActivity == level) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedActivity == level) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = level,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = description,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (isButtonPressed && selectedActivity.isEmpty()) {
                                Text(
                                    "Please select an activity level",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { currentStep = 3 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 2.dp
                                    )
                                ) {
                                    Text("Back", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        isButtonPressed = true
                                        if (selectedActivity.isNotEmpty()) {
                                            isButtonPressed = false
                                            currentStep = 5
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = Color(0xFFECEFF1)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 2.dp
                                    )
                                ) {
                                    Text("Next", fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    5 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your Goal",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Step 4 of 4",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            val goals = listOf(
                                "Lose a little weight" to "Gradual weight loss for better health",
                                "Lose a lot of weight" to "Significant weight loss for transformation",
                                "Maintain weight" to "Keep your current weight stable",
                                "Gain a little weight" to "Build some muscle or gain slight weight",
                                "Gain a lot of weight" to "Substantial muscle gain or bulking"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                goals.forEach { (goal, description) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedGoal = goal }
                                            .border(
                                                width = if (selectedGoal == goal) 2.dp else 1.dp,
                                                color = if (selectedGoal == goal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedGoal == goal) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = goal,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = description,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (isButtonPressed && selectedGoal.isEmpty()) {
                                Text(
                                    "Please select a goal",
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { currentStep = 4 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 2.dp
                                    )
                                ) {
                                    Text("Back", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        isButtonPressed = true
                                        if (selectedGoal.isNotEmpty()) {
                                            isButtonPressed = false
                                            authViewModel.completeOnboarding(
                                                firstName = firstName,
                                                lastName = lastName,
                                                birthDate = birthDate,
                                                selectedGender = selectedGender,
                                                height = height,
                                                weight = weight,
                                                selectedActivity = selectedActivity,
                                                selectedGoal = selectedGoal,
                                                context
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = Color(0xFFECEFF1)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 12.dp,
                                        disabledElevation = 2.dp
                                    )
                                ) {
                                    Text("Finish!", fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

