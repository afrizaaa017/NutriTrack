package com.example.nutritrack.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.example.nutritrack.data.api.RetrofitClient
import com.example.nutritrack.data.model.OnboardingResponse
import com.example.nutritrack.data.model.SignInResponse
import com.example.nutritrack.data.model.SignOutResponse
import com.example.nutritrack.data.model.SignUpResponse
import com.example.nutritrack.data.model.User
import com.example.nutritrack.data.model.UserProfile
import com.example.nutritrack.data.model.ResetUpdateResponse
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val sharedPref: SharedPreferences =
        getApplication<Application>().getSharedPreferences("app_prefs", Application.MODE_PRIVATE)

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val user = auth.currentUser
        if (user != null) {
            val isFirstLogin = isFirstLogin(user.uid)
            _authState.postValue(if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated)
        } else {
            _authState.postValue(AuthState.Unauthenticated)
        }
    }

    fun signIn(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        Log.d("AuthProcess", "Starting sign-in process")
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("AuthProcess", "Firebase authentication successful")
                    user?.let {
                        if (!it.isEmailVerified) {
                            _authState.value = AuthState.Error("Please verify your email before logging in")
                            sendEmailVerification(it, context)
                        } else {
                            val isFirstLogin = isFirstLogin(it.uid)
                            CoroutineScope(Dispatchers.Main).launch {
                                checkAndUpdatePassword(email, password) {
                                    sendSignInToBackend(email, password, context)
                                }
                            }
                            // _authState.value = if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                    Log.e("AuthProcess", "Authentication failed: ${task.exception?.message}")
                }
            }
    }


    fun checkAndUpdatePassword(email: String, newPassword: String, onSuccess: () -> Unit) {
        val requestBody = User(email, newPassword)

        RetrofitClient.instance.checkAndUpdatePassword(requestBody).enqueue(object : Callback<ResetUpdateResponse> {
            override fun onResponse(call: Call<ResetUpdateResponse>, response: Response<ResetUpdateResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Unknown response"
                    Log.d("PasswordUpdate", message)
                    onSuccess()
                } else {
                    Log.e("PasswordUpdate", "Failed to check/update password")
                }
            }

            override fun onFailure(call: Call<ResetUpdateResponse>, t: Throwable) {
                Log.e("PasswordUpdate", "Error: ${t.message}")
            }
        })
    }


    private fun sendSignInToBackend(email: String, password: String, context: Context) {
        val requestBody = User(email, password)
        Log.d("AuthProcess", "Sending sign-in request to backend")

        RetrofitClient.instance.signIn(requestBody).enqueue(object : Callback<SignInResponse> {
            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                Log.d("AuthProcess", "Backend response received: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("AuthProcess", "Token received: ${responseBody?.token}")
                    if (responseBody != null) {
                        sharedPref.edit().putString("auth_token", responseBody.token).apply()

                        val user = auth.currentUser
                        if (user != null) {
                            val isFirstLogin = isFirstLogin(user.uid)
                            Toast.makeText(context, "Sign in successfully", Toast.LENGTH_SHORT).show()
                            _authState.value = if (isFirstLogin) AuthState.Onboarding else AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.Error("User data is null")
                        }
                    } else {
                        _authState.value = AuthState.Error("Invalid response from backend")
                    }
                } else {
                    Log.e("AuthProcess", "Backend sign-in failed: ${response.errorBody()?.string()}")
                    _authState.value = AuthState.Error("Failed to sign in to backend")
                }
            }

            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                Log.e("AuthProcess", "Network error: ${t.message}")
                _authState.value = AuthState.Error(t.message ?: "Network error")
            }
        })
    }

    fun signUp(email: String, password: String, context: Context) {
        if (email.isEmpty() || password.isEmpty()) {
            Log.e("Auth", "Email and password cannot be empty")
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        registerUserInFirebase(email, password) { firebaseUser, error ->
            if (firebaseUser != null) {
                Log.d("Auth", "Firebase account created for $email, proceeding to Laravel")

                val user = User(email, password)
                Log.d("Auth", "Sending sign-up request to Laravel for email: $email")

                RetrofitClient.instance.signUp(user).enqueue(object : Callback<SignUpResponse> {
                    override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                        Log.d("Auth", "Backend response received: Code=${response.code()}, Body=${response.body()}")

                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null && responseBody.success) {
                                Log.d("Auth", "Backend sign-up successful")
                                sendEmailVerification(firebaseUser, context)
                                setFirstLogin(firebaseUser.uid)
                                _authState.value = AuthState.SignUp
                            } else {
                                val errorMsg = responseBody?.message ?: "Unknown error from backend"
                                Log.e("Auth", "Backend sign-up failed: $errorMsg")
                                Toast.makeText(context, "Failed to sign up. Backend error.", Toast.LENGTH_LONG).show()
                                firebaseUser.delete().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        Log.d("Auth", "Firebase account deleted due to backend failure")
                                    } else {
                                        Log.e("Auth", "Failed to delete Firebase account: ${deleteTask.exception?.message}")
                                    }
                                }
                                _authState.value = AuthState.Error(errorMsg)
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("Auth", "Failed to connect to backend. Response Code: ${response.code()}, Error Body: $errorBody")
                            Toast.makeText(context, "Failed to sign up. Backend error.", Toast.LENGTH_LONG).show()
                            firebaseUser.delete().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Log.d("Auth", "Firebase account deleted due to backend error")
                                } else {
                                    Log.e("Auth", "Failed to delete Firebase account: ${deleteTask.exception?.message}")
                                }
                            }
                            _authState.value = AuthState.Error("Sign up failed: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                        Log.e("Auth", "Network error during backend sign-up: ${t.message}", t)
                        Toast.makeText(context, "Failed to sign up. Network error.", Toast.LENGTH_LONG).show()
                        firebaseUser.delete().addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                Log.d("Auth", "Firebase account deleted due to network error")
                            } else {
                                Log.e("Auth", "Failed to delete Firebase account: ${deleteTask.exception?.message}")
                            }
                        }
                        _authState.value = AuthState.Error(t.message ?: "Network error")
                    }
                })
            } else {
                Log.e("Auth", "Firebase sign-up failed: $error")
                Toast.makeText(context, "Failed to sign up.", Toast.LENGTH_LONG).show()

                _authState.value = AuthState.Error(error ?: "Failed to check email availability")
            }
        }
    }

    private fun registerUserInFirebase(email: String, password: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        Log.d("Auth", "Firebase sign-up successful: UID=${it.uid}")
                        callback(user, null)
                    } ?: run {
                        Log.e("Auth", "Firebase user is null after sign-up")
                        callback(null, "Firebase user is null")
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        callback(null, "Email already registered")
                    } else {
                        val errorMessage = exception?.message ?: "Unknown Firebase error"
                        callback(null, errorMessage)
                    }
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser, context: Context) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "Email verification sent to ${user.email}")
                    Toast.makeText(context, "You need to verify your email. Please check your mailbox!", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = task.exception?.message ?: "Failed to send verification email"
                    Log.e("Auth", "Email verification failed: $errorMessage", task.exception)
                    _authState.value = AuthState.Error(errorMessage)
                }
            }
    }

//    private fun sendUserToBackend(email: String, password: String) {
//        val user = User(email, password)
//        Log.d("Auth", "Sending user data to backend: $user")
//
//        RetrofitClient.instance.signUp(user).enqueue(object : Callback<SignUpResponse> {
//            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
//                Log.d("Auth", "Backend response received: Code=${response.code()}, Body=${response.body()}")
//
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null && responseBody.success) {
//                        Log.d("Auth", "Backend sign-up successful")
//                        _authState.value = AuthState.SignUp
//                    } else {
//                        val errorMsg = responseBody?.message ?: "Unknown error from backend"
//                        Log.e("Auth", "Backend sign-up failed: $errorMsg")
//                        _authState.value = AuthState.Error(errorMsg)
//                    }
//                } else {
//                    Log.e("Auth", "Failed to connect to backend. Response Code: ${response.code()}, Error Body: ${response.errorBody()?.string()}")
//                    _authState.value = AuthState.Error("Failed to connect to backend")
//                }
//            }
//
//            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
//                Log.e("Auth", "Network error during backend sign-up: ${t.message}", t)
//                _authState.value = AuthState.Error(t.message ?: "Network error")
//            }
//        })
//    }

    fun signOut(context: Context) {
        val token = getToken()?: ""
        Log.d("SignOut", "Retrieved token: $token")

        if (token.isEmpty()) {
            Log.d("SignOut", "Token is empty, setting state to Unauthenticated")
            _authState.value = AuthState.Unauthenticated
            return
        }

        Log.d("SignOut", "Sending sign-out request to backend")
        RetrofitClient.instance.signOut("Bearer $token").enqueue(object : Callback<SignOutResponse> {
            override fun onResponse(call: Call<SignOutResponse>, response: Response<SignOutResponse>) {
                Log.d("SignOut", "Response received from backend: ${response.code()}")

                sharedPref.edit().remove("auth_token").apply()
                Log.d("SignOut", "Token removed from SharedPreferences")

                if (response.isSuccessful) {
                    Log.d("SignOut", "Backend sign-out successful, signing out from Firebase")
                    auth.signOut()
                    Toast.makeText(context, "Sign out successfully", Toast.LENGTH_SHORT).show()
                    _authState.value = AuthState.Unauthenticated
                } else {
                    Log.e("SignOut", "Backend sign-out failed with code: ${response.code()}")
                    _authState.value = AuthState.Error("Failed to sign out")
                }
            }

            override fun onFailure(call: Call<SignOutResponse>, t: Throwable) {
                Log.e("SignOut", "Network error: ${t.message}")
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


    fun completeOnboarding(
        firstName: String,
        lastName: String,
        birthDate: String,
        selectedGender: String,
        height: String,
        weight: String,
        selectedActivity: String,
        selectedGoal: String,
        context: Context
    ) {
        val user = auth.currentUser
        user?.let {
            val email = user.email ?: ""

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthday: String = try {
                isoFormat.format(dateFormat.parse(birthDate)!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Invalid date format")
                return
            }

            val heightFloat = height.toFloatOrNull()
            val weightFloat = weight.toFloatOrNull()

            if (heightFloat == null || weightFloat == null) {
                _authState.value = AuthState.Error("Invalid height or weight")
                return
            }

            val genderBoolean = when (selectedGender.lowercase()) {
                "male" -> true
                "female" -> false
                else -> false
            }

            val profile = UserProfile(
                email = email,
                firstName = firstName,
                lastName = lastName,
                birthday = birthday,
                weight = weightFloat,
                height = heightFloat,
                goal = selectedGoal,
                amr = selectedActivity,
                caloriesNeeded = 0f,
                gender = genderBoolean,
                image = null
            )

            val token = getToken() ?: ""
            if (token.isEmpty()) {
                _authState.value = AuthState.Error("User not authenticated")
                return
            }

            RetrofitClient.instance.completeOnboarding("Bearer $token", profile)
                .enqueue(object : Callback<OnboardingResponse> {
                    override fun onResponse(call: Call<OnboardingResponse>, response: Response<OnboardingResponse>) {
                        if (response.isSuccessful) {
                            sharedPref.edit().putBoolean("onboarding_${user.uid}", false).apply()
                            Toast.makeText(context, "You're all set! Your journey starts now. Enjoy exploring our app!", Toast.LENGTH_SHORT).show()
                            _authState.value = AuthState.Authenticated
                        } else {
                            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                            _authState.value = AuthState.Error("Failed to save onboarding data $errorMessage")
                        }
                    }

                    override fun onFailure(call: Call<OnboardingResponse>, t: Throwable) {
                        _authState.value = AuthState.Error(t.message ?: "Network error")
                    }
                })
        }
    }



    fun getToken(): String? {
        return sharedPref.getString("auth_token", null)
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