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
import com.example.nutritrack.ui.auth.AuthViewModel
import com.example.nutritrack.ui.theme.BrokenWhite

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
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentStep) {
            1 -> {
                Text("Let us know you!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Fill in your details to personalize your experience!", fontSize = 16.sp)
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
                        disabledContainerColor = BrokenWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp,
                        disabledElevation = 2.dp
                    )
                ) {
                    Text("Let’s Begin!")
                }
            }

            2 -> {
                Text("Step 1: Personal Information", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(48.dp))

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
                    Text("First Name cannot be empty", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

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
                    Text("Last Name cannot be empty", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                    Text("Date of Birth cannot be empty", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))

                Text("Gender", fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Male", "Female").forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedGender = gender }
                        ) {
                            RadioButton(
                                selected = selectedGender == gender,
                                onClick = { selectedGender = gender },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(gender, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                if (isButtonPressed && selectedGender.isEmpty()) {
                    Text("Please select a gender", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isButtonPressed = true
                        if (firstName.isNotEmpty() && lastName.isNotEmpty() && birthDate.isNotEmpty()) {
                            isButtonPressed = false
                            currentStep = 3
                        }
                    },
                    modifier = Modifier
                        .width(280.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = BrokenWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp,
                        disabledElevation = 2.dp
                    )
                ) {
                    Text("Next")
                }
            }

            3 -> {
                Text("Step 2: Body Measurements", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(48.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                if (isButtonPressed && height.isEmpty()) {
                    Text("Height cannot be empty", color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = isButtonPressed && weight.isEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                if (isButtonPressed && weight.isEmpty()) {
                    Text("Weight cannot be empty", color = Color.Red, fontSize = 12.sp)
                }
            }
                Spacer(modifier = Modifier.height(24.dp))
                Row (
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
                        Text("Back")
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
                            disabledContainerColor = BrokenWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            disabledElevation = 2.dp
                        )
                    ) {
                        Text("Next")
                    }
                }
            }
            4 -> {
                Text("Step 3: Activity Level", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(48.dp))

                Column {
                    listOf("Sedentary active", "Lightly active", "Moderately active", "Highly active", "Extremely active").forEach { level ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedActivity == level, onClick = { selectedActivity = level })
                            Text(level, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(onClick = { currentStep = 3 },
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
                        )) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        isButtonPressed = true
                        if (selectedActivity.isNotEmpty()) {
                            isButtonPressed = false
                            currentStep = 5
                        } },
                        modifier = Modifier
                            .weight(1f)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = BrokenWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            disabledElevation = 2.dp
                        )) {
                        Text("Next")
                    }
                }
            }

            5 -> {
                Text("Step 4: Choose Your Goal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(48.dp))

                Column {
                    listOf("Lose a little weight", "Lose a lot of weight", "Maintain weight", "Gain a little weight", "Gain a lot of weight").forEach { goal ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedGoal == goal, onClick = { selectedGoal = goal })
                            Text(goal, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        Text("Back")
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
                            disabledContainerColor = BrokenWhite
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            disabledElevation = 2.dp
                        )
                    ) {
                        Text("Finish!")
                    }
                }
            }
        }
    }
}

