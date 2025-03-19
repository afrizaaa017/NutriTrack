package com.example.nutritrack.ui.navigation

import com.example.nutritrack.R

sealed class BottomNavItem(val route: String, val title: String, val icon: Int) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", R.drawable.ic_dashboard)
    object Eats : BottomNavItem("eats", "Eats", R.drawable.ic_eats)
    object Leaderboard : BottomNavItem("leaderboard", "Leaderboard", R.drawable.ic_leaderboard)
    object Profile : BottomNavItem("profile", "Profile", R.drawable.ic_profile)
}
