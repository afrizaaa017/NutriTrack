package com.example.nutritrack.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutritrack.ui.auth.AuthState
import com.example.nutritrack.ui.auth.AuthViewModel
import kotlinx.coroutines.delay


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.observeAsState()
    val currentAuthState by rememberUpdatedState(authState)
    val context = LocalContext.current

    LaunchedEffect(currentAuthState) {
        if (currentAuthState is AuthState.Unauthenticated) {
            Toast.makeText(context, "Signed out!", Toast.LENGTH_SHORT).show()
            delay(1500)
            navController.navigate("signin") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile", fontSize = 32.sp)

        TextButton(onClick = {
            authViewModel.signOut()
        }) {
            Text(text = "Sign Out")
        }
    }
}