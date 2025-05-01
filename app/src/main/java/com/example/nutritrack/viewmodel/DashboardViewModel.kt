package com.example.nutritrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.model.DailySummary
import com.example.nutritrack.data.repository.DashboardRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: DashboardRepository, private val firebaseAuth: FirebaseAuth) : ViewModel() {
    private val _dailyReportState = MutableStateFlow<DailyReportState>(DailyReportState.Initial)
    val dailyReportState: StateFlow<DailyReportState> = _dailyReportState.asStateFlow()


    // Sealed class for UI state
    sealed class DailyReportState {
        object Initial : DailyReportState()
        object Loading : DailyReportState()
        data class Success(val dailySummary: DailySummary) : DailyReportState()
        data class Error(val message: String) : DailyReportState()
    }

    fun fetchDailyReport(authToken: String, date: String) {
        viewModelScope.launch {
            _dailyReportState.value = DailyReportState.Loading
            try {
                val response = repository.getDailyReport(authToken, date)
                val userEmail = firebaseAuth.currentUser?.email ?: ""
                val dailySummary = response.data ?: DailySummary(
                    email = userEmail,
                    date = date,
                    targetCalories = 0,
                    caloriesConsumed = 0,
                    fatConsumed = 0,
                    sugarConsumed = 0,
                    carbsConsumed = 0,
                    proteinConsumed = 0,
                    weightRecap = 0f,
                    heightRecap = 0f
                )
                _dailyReportState.value = DailyReportState.Success(dailySummary)
            } catch (e: Exception) {
                _dailyReportState.value = DailyReportState.Error(
                    e.message ?: "Failed to fetch daily report"
                )
            }
        }
    }

    fun setErrorState(message: String) {
        _dailyReportState.value = DailyReportState.Error(message)
    }
}