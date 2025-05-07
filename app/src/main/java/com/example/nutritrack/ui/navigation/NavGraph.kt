package com.example.nutritrack.ui.navigation

import OnboardingScreen
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.nutritrack.MainScreen
import com.example.nutritrack.viewmodel.AuthViewModel
import com.example.nutritrack.viewmodel.AuthState
import com.example.nutritrack.ui.auth.ForgotPasswordScreen
import com.example.nutritrack.ui.auth.SignInScreen
import com.example.nutritrack.ui.auth.SignUpScreen
import com.example.nutritrack.ui.screen.dashboard.DashboardScreen
import com.example.nutritrack.ui.screen.eats.FoodScreen
import com.example.nutritrack.ui.screen.leaderboard.LeaderboardScreen
import com.example.nutritrack.ui.screen.profile.ProfileScreen
import com.example.nutritrack.ui.screen.eats.EatsScreen
import com.example.nutritrack.ui.screen.eats.FoodList

@Composable
fun RootNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current

    when (authState) {
        is AuthState.Authenticated -> {
            MainScreen(navController, authViewModel)
        }
        is AuthState.Onboarding -> {
            OnboardingScreen(navController, authViewModel)
        }
        is AuthState.Unauthenticated, null -> {
            AuthNavGraph(navController, authViewModel)
        }
        is AuthState.SignUp -> {
            authViewModel.resetAuthState()
            navController.navigate("signin") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
        is AuthState.Error -> {
            Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            authViewModel.resetAuthState()
        }
        is AuthState.Loading -> Unit
    }
}

@Composable
fun AuthNavGraph(navController: NavHostController, authViewModel: AuthViewModel) {
    val modifier = Modifier
    NavHost(navController = navController, startDestination = "signin") {
        composable("signin") { SignInScreen(modifier, navController, authViewModel) }
        composable("signup") { SignUpScreen(modifier, navController, authViewModel) }
        composable("forgotpassword") { ForgotPasswordScreen(navController, authViewModel) }
    }
}

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Dashboard.route) { DashboardScreen(authViewModel) }
        composable(BottomNavItem.Eats.route) { EatsScreen(navController) }
        composable(BottomNavItem.Leaderboard.route) { LeaderboardScreen() }
        composable(BottomNavItem.Profile.route) { ProfileScreen(modifier, navController, authViewModel) }

        composable(
            "foodscreen/{mealType}",
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "unknown"
            FoodScreen(navController, mealType, authViewModel)
        }
    }
}

