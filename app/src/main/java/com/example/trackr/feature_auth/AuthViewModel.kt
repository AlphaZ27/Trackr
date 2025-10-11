package com.example.trackr.feature_auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.feature_settings.domain.model.UserType
import com.example.trackr.feature_settings.domain.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthScreenState>(AuthScreenState.Idle)
    val authState = _authState.asStateFlow()

//    fun loginUser(email: String, password: String) {
//        viewModelScope.launch {
//            _authState.value = AuthScreenState.Loading
//            authRepository.login(email, password)
//                .onSuccess { _authState.value = AuthScreenState.Success }
//                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Unknown login error") }
//        }
//    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val role = document.getString("role") ?: "User"

                    val userType = when (role) {
                        "Admin" -> UserType.Admin
                        "Manager" -> UserType.Manager
                        else -> UserType.User
                    }

                    accountRepository.saveUserType(userType)
                    // Set the Success state with the user's role
                    _authState.value = AuthScreenState.Success(userType)
                } else {
                    _authState.value = AuthScreenState.Error("Login failed. Please try again.")
                }
            } catch (e: Exception) {
                _authState.value = AuthScreenState.Error(e.localizedMessage ?: "An unknown error occurred.")
            }
        }
    }

    // New function to handle the registration state
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            authRepository.register(email, password)
                .onSuccess { _authState.value = AuthScreenState.Success(userType = UserType.User) }
                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Registration failed") }
        }
    }

    // New function to handle the password reset state
    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthScreenState.Loading
            // Using a general Success state. Can be customized if a specific message is needed.
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _authState.value = AuthScreenState.Success(userType = UserType.User) }
                .onFailure { _authState.value = AuthScreenState.Error(it.message ?: "Failed to send reset email") }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthScreenState.Idle
    }

    fun logoutUser() {
        authRepository.logout()
    }
}