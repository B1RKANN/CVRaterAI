package com.cvraterai.myapplication.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.JwtUtil
import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.api.ProfileApiService
import com.cvraterai.myapplication.data.model.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApiService: ProfileApiService,
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
        _loading.value = true
        _error.value = null
        
        // Get access token from TokenManager
        val accessToken = tokenManager.getAccessToken()
        if (accessToken == null) {
            _error.value = "No access token found"
            _loading.value = false
            return
        }
        
        // Extract user ID from token
        val userId = JwtUtil.getUserIdFromToken(accessToken)
        if (userId == null) {
            _error.value = "Could not extract user ID from token"
            _loading.value = false
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching profile data for user ID: $userId")
                val response = profileApiService.getProfile(userId)
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    val profileData = apiResponse.payload
                    
                    if (apiResponse.status == 200 && profileData != null) {
                        _profileData.value = profileData
                        Log.d(TAG, "Profile data fetched successfully: $profileData")
                    } else {
                        _error.value = apiResponse.errorMessage ?: "Unknown error occurred"
                        Log.e(TAG, "API returned error: ${apiResponse.errorMessage}")
                    }
                } else {
                    _error.value = "Failed to fetch profile data: ${response.message()}"
                    Log.e(TAG, "Failed to fetch profile data: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e(TAG, "Error fetching profile data", e)
            } finally {
                _loading.value = false
            }
        }
    }
} 