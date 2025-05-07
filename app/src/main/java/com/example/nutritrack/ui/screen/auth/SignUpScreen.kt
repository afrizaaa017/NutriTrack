package com.example.nutritrack.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutritrack.viewmodel.AuthViewModel
import com.example.nutritrack.viewmodel.AuthState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val currentAuthState by rememberUpdatedState(authState)

    var isEmailFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isConfirmPasswordFocused by remember { mutableStateOf(false) }

    val isEmailValid = email.isNotEmpty()
    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()

    LaunchedEffect(currentAuthState) {
        when (currentAuthState) {
            is AuthState.SignUp -> {
                // Toast.makeText(context, "Sign up successfully!", Toast.LENGTH_SHORT).show()
                // delay(1500)
                authViewModel.resetAuthState()
                navController.navigate("signin") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (currentAuthState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign Up",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Create your new account", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(280.dp),
            singleLine = true,
            visualTransformation = VisualTransformation.None,
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

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(thickness = 1.2.dp, color = Color.Gray, modifier = Modifier.width(280.dp))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordFocused = true
            },
            label = { Text(text = "Password") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(280.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = if (!isPasswordFocused || isPasswordValid) MaterialTheme.colorScheme.tertiary else Color.Red,
                focusedBorderColor = if (isPasswordValid) MaterialTheme.colorScheme.primary else Color.Red
            ),
            isError = isPasswordFocused && !isPasswordValid,
            visualTransformation = PasswordVisualTransformation()
        )

        if (isPasswordFocused && !isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                isConfirmPasswordFocused = true
            },
            label = { Text(text = "Confirm Password") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(280.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                unfocusedBorderColor = if (!isConfirmPasswordFocused || isConfirmPasswordValid) MaterialTheme.colorScheme.tertiary else Color.Red,
                focusedBorderColor = if (isConfirmPasswordValid) MaterialTheme.colorScheme.primary else Color.Red
            ),
            isError = isConfirmPasswordFocused && !isConfirmPasswordValid,
            visualTransformation = PasswordVisualTransformation()
        )

        if (isConfirmPasswordFocused && !isConfirmPasswordValid) {
            Text(
                text = "Passwords do not match",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                isEmailFocused = true
                isPasswordFocused = true
                isConfirmPasswordFocused = true

                if (isEmailValid && isPasswordValid && isConfirmPasswordValid) {
                    if (authState !is AuthState.Loading) {
                        authViewModel.signUp(email, password, context)
                    }
                }
            },
            enabled = authState !is AuthState.Loading,
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
                text = "Sign Up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signin") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }) {
            Text(text = "Already have an account? Sign In")
        }
    }
}
