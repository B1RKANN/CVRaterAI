package com.cvraterai.myapplication.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.JwtUtil
import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.api.AuthApiService
import com.cvraterai.myapplication.data.api.ProfileApiService
import com.cvraterai.myapplication.data.model.ProfileResponse
import com.cvraterai.myapplication.data.model.RefreshTokenRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val TAG = "ProfileViewModel"
    
    private val _profileData = MutableLiveData<ProfileResponse>()
    val profileData: LiveData<ProfileResponse> = _profileData
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                // Access token'dan user ID'yi almaya çalış
                val accessToken = tokenManager.getAccessToken()
                Log.d(TAG, "Current access token: ${accessToken != null}")
                
                val userId = if (accessToken != null) {
                    // Access token'dan user ID'yi çıkar
                    JwtUtil.getUserIdFromToken(accessToken)?.also {
                        Log.d(TAG, "Extracted user ID from token: $it")
                    } ?: run {
                        Log.d(TAG, "Could not extract user ID from token, using default")
                        1 // Default değer
                    }
                } else {
                    // Access token yoksa ve refresh token varsa, default olarak 1 kullan
                    // Bu durumda Authenticator token yenilemeyi halledecek
                    if (tokenManager.getRefreshToken() != null) {
                        Log.d(TAG, "No access token but refresh token exists, using default user ID")
                        1
                    } else {
                        Log.e(TAG, "No tokens available")
                        throw Exception("No tokens available")
                    }
                }
                
                Log.d(TAG, "Fetching profile data for user ID: $userId")
                val response = profileApiService.getProfile(userId)
                
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.status == 200) {
                            _profileData.value = apiResponse.payload
                            Log.d(TAG, "Profile data fetched successfully: ${apiResponse.payload}")
                        } else {
                            _error.value = apiResponse.errorMessage ?: "Unknown error occurred"
                            Log.e(TAG, "API returned error: ${apiResponse.errorMessage}")
                        }
                    }
                } else {
                    if (response.code() == 401) {
                        Log.d(TAG, "Received 401, token will be refreshed automatically")
                        // 401 aldık, Authenticator token yenilemeyi otomatik halledecek
                    } else {
                        _error.value = "Failed to load profile: ${response.code()}"
                        Log.e(TAG, "Failed to load profile: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error loading profile: ${e.message}"
                Log.e(TAG, "Error loading profile", e)
            } finally {
                _loading.value = false
            }
        }
    }
} 