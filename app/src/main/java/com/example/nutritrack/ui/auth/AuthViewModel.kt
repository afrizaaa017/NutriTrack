package com.example.nutritrack.ui.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.SignInResponse
import com.example.nutritrack.data.model.SignOutResponse
import com.example.nutritrack.data.model.SignUpResponse
import com.example.nutritrack.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
                    user?.let {
                        val isFirstLogin = isFirstLogin(it.uid)
                        sendSignInToBackend(email, password)
                        // _authState.value = if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    private fun sendSignInToBackend(email: String, password: String) {
        val requestBody = User(email, password)

        RetrofitClient.instance.signIn(requestBody).enqueue(object : Callback<SignInResponse> {
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        sharedPref.edit().putString("auth_token", responseBody.token).apply()

                        val user = auth.currentUser
                        if (user != null) {
                            val isFirstLogin = isFirstLogin(user.uid)
                            _authState.value = if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.Error("User data is null")
                        }
                    } else {
                        _authState.value = AuthState.Error("Invalid response from backend")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to sign in to backend")
                }
            }

            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                _authState.value = AuthState.Error(t.message ?: "Network error")
            }
        })
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
                        sendUserToBackend(it.email ?: "", password)
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    private fun sendUserToBackend(email: String, password: String) {
        val user = User(email, password)
        RetrofitClient.instance.signUp(user).enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.success) {
                        _authState.value = AuthState.SignUp
                    } else {
                        _authState.value = AuthState.Error(responseBody?.message ?: "Unknown error from backend")
                    }
                } else {
                    _authState.value = AuthState.Error("Failed to connect to backend")
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                _authState.value = AuthState.Error(t.message ?: "Network error")
            }
        })
    }

    fun signOut() {
        val token = sharedPref.getString("auth_token", "") ?: ""
        if (token.isEmpty()) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        RetrofitClient.instance.signOut("Bearer $token").enqueue(object : Callback<SignOutResponse> {
            override fun onResponse(call: Call<SignOutResponse>, response: Response<SignOutResponse>) {
                // Hapus token dari SharedPreferences
                sharedPref.edit().remove("auth_token").apply()

                if (response.isSuccessful) {
                    auth.signOut()
                    _authState.value = AuthState.Unauthenticated
                } else {
                    _authState.value = AuthState.Error("Failed to sign out")
                }
            }

            override fun onFailure(call: Call<SignOutResponse>, t: Throwable) {
                _authState.value = AuthState.Error(t.message ?: "Network error")
            }
        })
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }

    // forgot password
    fun sendPasswordResetEmail(email: String, context: Context) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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