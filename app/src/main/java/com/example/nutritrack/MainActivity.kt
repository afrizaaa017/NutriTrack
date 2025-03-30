package com.example.nutritrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.nutritrack.ui.auth.AuthViewModel
import com.example.nutritrack.ui.navigation.BottomNavigationBar
import com.example.nutritrack.ui.navigation.NavGraph
import com.example.nutritrack.ui.navigation.RootNavGraph
import com.example.nutritrack.ui.theme.NutriTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriTrackTheme {
                val authViewModel: AuthViewModel = viewModel()
                RootNavGraph(authViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavGraph(navController, Modifier.padding(innerPadding), authViewModel = viewModel())
    }
}