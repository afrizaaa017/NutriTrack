package com.example.nutritrack.ui.auth

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutritrack.ui.auth.AuthState
import com.example.nutritrack.ui.auth.AuthViewModel
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

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val currentAuthState by rememberUpdatedState(authState)

    LaunchedEffect(currentAuthState) {
        println("Current Auth State: $currentAuthState")
        when (currentAuthState) {
            is AuthState.SignUp -> {
                Toast.makeText(context, " sign up Successfully!", Toast.LENGTH_SHORT).show()
                delay(1500)
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
        Text(text = "Sign Up", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Create your new account", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                containerColor = MaterialTheme.colorScheme.tertiary,
//                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
//            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            shape = RoundedCornerShape(12.dp),
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                containerColor = MaterialTheme.colorScheme.tertiary,
//                unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
//            )
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                if (authState !is AuthState.Loading) {
                    authViewModel.signUp(email, password)
                }
            },
            enabled = authState !is AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = 40.dp)
        ) {
            Text(text = "Create Account")
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
