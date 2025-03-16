package com.cvraterai.myapplication.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.model.AuthResponse
import com.cvraterai.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val TAG = "LoginViewModel"
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email ve şifre boş olamaz")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            
            _loginState.value = when {
                result.isSuccess -> {
                    val authResponse = result.getOrNull()!!
                    // Token'ları logcat'te göster
                    Log.d(TAG, "Access Token: ${authResponse.accessToken}")
                    Log.d(TAG, "Refresh Token: ${authResponse.refreshToken}")
                    Log.d(TAG, "Token: ${authResponse.token}")
                    Log.d(TAG, "Access Token (alt): ${authResponse.access_token}")
                    Log.d(TAG, "Refresh Token (alt): ${authResponse.refresh_token}")
                    Log.d(TAG, "ID Token: ${authResponse.idToken}")
                    
                    // Yardımcı metodları kullanalım
                    val effectiveAccessToken = authResponse.getEffectiveAccessToken()
                    val effectiveRefreshToken = authResponse.getEffectiveRefreshToken()
                    Log.d(TAG, "Effective Access Token: $effectiveAccessToken")
                    Log.d(TAG, "Effective Refresh Token: $effectiveRefreshToken")
                    
                    // Ayrıca TokenManager'dan da kontrol edelim
                    val savedAccessToken = authRepository.getAccessToken()
                    val savedRefreshToken = authRepository.getRefreshToken()
                    Log.d(TAG, "Saved Access Token: $savedAccessToken")
                    Log.d(TAG, "Saved Refresh Token: $savedRefreshToken")
                    
                    LoginState.Success(authResponse)
                }
                else -> LoginState.Error(result.exceptionOrNull()?.message ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }
    
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    // TokenManager'dan token'ları almak için yardımcı metodlar
    fun getAccessToken(): String? {
        return authRepository.getAccessToken()
    }
    
    fun getRefreshToken(): String? {
        return authRepository.getRefreshToken()
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val data: AuthResponse) : LoginState()
    data class Error(val message: String) : LoginState()
} 