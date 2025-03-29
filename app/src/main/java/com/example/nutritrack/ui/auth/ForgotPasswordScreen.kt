package com.example.nutritrack.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    var isResendEnabled by remember { mutableStateOf(true) }
    var countdownTime by remember { mutableStateOf(0) }
    var isEmailFocused by remember { mutableStateOf(false) }

    val isEmailValid = email.isNotEmpty()

    LaunchedEffect(countdownTime) {
        if (countdownTime > 0) {
            delay(1000L)
            countdownTime--
            if (countdownTime == 0) {
                isResendEnabled = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Forgot Password", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Enter your email to reset your password", fontSize = 14.sp)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                isEmailFocused = true
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(280.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = if (!isEmailFocused || isEmailValid) MaterialTheme.colorScheme.tertiary else Color.Red,
                focusedBorderColor = if (isEmailValid) MaterialTheme.colorScheme.primary else Color.Red
            ),
            isError = isEmailFocused && !isEmailValid
        )

        if (isEmailFocused && !isEmailValid) {
            Text(
                text = "Email cannot be empty",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isEmailValid) {
                    authViewModel.sendPasswordResetEmail(email, context)
                } else {
                    isEmailFocused = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 8.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp,
                disabledElevation = 2.dp
            )
        ) {
            Text(
                text = "Send Reset Password",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                if (isResendEnabled) {
                    if (isEmailValid) {
                        authViewModel.sendPasswordResetEmail(email, context)
                        isResendEnabled = false
                        countdownTime = 60
                    } else {
                        isEmailFocused = true
                    }
                }
            },
            enabled = isResendEnabled
        ) {
            Text(
                text = if (isResendEnabled) "Didn't receive email?" else "Resend email in $countdownTime s",
                color = if (isResendEnabled) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("signin") }) {
            Text("Back to Sign In")
        }
    }
}



