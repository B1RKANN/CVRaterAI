package com.cvraterai.myapplication.ui.auth

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState
    
    fun register(name: String, surname: String, email: String, password: String) {
        if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("Tüm alanlar doldurulmalıdır")
            return
        }
        
        if (!isValidEmail(email)) {
            _registerState.value = RegisterState.Error("Geçerli bir email adresi giriniz")
            return
        }
        
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Şifre en az 6 karakter olmalıdır")
            return
        }
        
        _registerState.value = RegisterState.Loading
        
        val fullName = "$name $surname".trim()
        
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password)
            
            _registerState.value = when {
                result.isSuccess -> RegisterState.Success(result.getOrNull()!!)
                else -> RegisterState.Error(result.exceptionOrNull()?.message ?: "Bilinmeyen bir hata oluştu")
            }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val data: AuthResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 