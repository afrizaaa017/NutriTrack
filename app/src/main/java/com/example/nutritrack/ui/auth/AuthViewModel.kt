package com.example.nutritrack.ui.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val sharedPref: SharedPreferences =
        getApplication<Application>().getSharedPreferences("app_prefs", Application.MODE_PRIVATE)

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        if (user != null) {
            val isFirstLogin = isFirstLogin(user.uid)
            _authState.postValue(if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated)
        } else {
            _authState.postValue(AuthState.Unauthenticated)
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val isFirstLogin = isFirstLogin(user.uid)
                        _authState.value = if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signUp(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        setFirstLogin(it.uid)
                        _authState.value = AuthState.SignUp
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }

    fun sendPasswordResetEmail(email: String, context: Context) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Link reset password has been sent to your email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed send reset password to : ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun completeOnboarding() {
        val user = auth.currentUser
        user?.let {
            sharedPref.edit().putBoolean("onboarding_${user.uid}", false).apply()
            _authState.value = AuthState.Authenticated
        }
    }

    private fun isFirstLogin(uid: String): Boolean {
        return sharedPref.getBoolean("onboarding_$uid", true)
    }
    private fun setFirstLogin(uid: String) {
        sharedPref.edit().putBoolean("onboarding_$uid", true).apply()
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Onboarding : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    object SignUp : AuthState()
    data class Error(val message: String) : AuthState()

}