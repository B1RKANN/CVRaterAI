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
import com.cvraterai.myapplication.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileApiService: ProfileApiService,
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val TAG = "ProfileViewModel"
    
    private val _profileData = MutableLiveData<ProfileResponse>()
    val profileData: LiveData<ProfileResponse> = _profileData
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Token yenileme testi sonuçları için LiveData
    private val _tokenTestResult = MutableLiveData<TokenTestResult>()
    val tokenTestResult: LiveData<TokenTestResult> = _tokenTestResult
    
    fun fetchProfileData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                // Önce token durumunu kontrol et ve gerekirse yenile
                val hasValidToken = withContext(Dispatchers.IO) {
                    authRepository.ensureValidAccessTokenSync()
                }
                
                if (!hasValidToken) {
                    Log.e(TAG, "Could not get a valid token, fetch profile aborted")
                    _error.value = "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
                    _loading.value = false
                    return@launch
                }
                
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
                    // Bu noktada access token olmalıydı çünkü yukarıda kontrol ettik ve yeniledik
                    // Eğer hala yoksa bir sorun var
                    Log.e(TAG, "No access token despite token validation")
                    throw Exception("No access token available")
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
                        Log.d(TAG, "Received 401, token should be refreshed automatically")
                        // 401 aldık, token yenilemeyi tekrar deneyelim
                        val refreshed = withContext(Dispatchers.IO) {
                            authRepository.ensureValidAccessTokenSync()
                        }
                        
                        if (refreshed) {
                            // Token yenilendi, isteği tekrar deneyelim
                            _error.value = "Token yenilendi, lütfen tekrar deneyin."
                        } else {
                            _error.value = "Oturum süresi dolmuş. Lütfen tekrar giriş yapın."
                        }
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
    
    // Token yenileme testi
    fun testTokenRefresh() {
        viewModelScope.launch {
            _tokenTestResult.value = TokenTestResult.Loading
            
            try {
                // Mevcut token durumlarını log'a yaz
                val accessTokenBefore = tokenManager.getAccessToken()
                val refreshTokenBefore = tokenManager.getRefreshToken()
                
                Log.d(TAG, "TEST - Before refresh - Access Token: ${accessTokenBefore?.take(10) ?: "null"}...")
                Log.d(TAG, "TEST - Before refresh - Refresh Token: ${refreshTokenBefore?.take(10) ?: "null"}...")
                
                // Refresh token yoksa testi sonlandır
                if (refreshTokenBefore == null) {
                    _tokenTestResult.value = TokenTestResult.Error("Refresh token bulunamadı. Lütfen tekrar giriş yapın.")
                    return@launch
                }
                
                // Access token'ı manuel olarak temizle (test için)
                if (accessTokenBefore != null) {
                    tokenManager.clearAccessToken()
                    Log.d(TAG, "TEST - Access token cleared for testing")
                } else {
                    Log.d(TAG, "TEST - Access token already null, no need to clear")
                }
                
                // Senkron olarak token yenileme işlemini çağır
                // withContext kullanarak IO dispatcher'da çalıştır
                val refreshResult = withContext(Dispatchers.IO) {
                    authRepository.ensureValidAccessTokenSync()
                }
                
                // Yenileme sonrası token durumlarını kontrol et
                val accessTokenAfter = tokenManager.getAccessToken()
                val refreshTokenAfter = tokenManager.getRefreshToken()
                
                Log.d(TAG, "TEST - After refresh - Access Token: ${accessTokenAfter?.take(10) ?: "null"}...")
                Log.d(TAG, "TEST - After refresh - Refresh Token: ${refreshTokenAfter?.take(10) ?: "null"}...")
                Log.d(TAG, "TEST - Token refresh result: $refreshResult")
                
                if (refreshResult && accessTokenAfter != null) {
                    // Token yenileme başarılı
                    _tokenTestResult.value = TokenTestResult.Success(
                        "Token yenileme başarılı!\n\n" +
                        "Önceki Access Token: ${if (accessTokenBefore == null) "Yok" else accessTokenBefore.take(10) + "..."}\n" +
                        "Yeni Access Token: ${accessTokenAfter.take(10)}...\n\n" +
                        "Refresh Token: ${refreshTokenAfter?.take(10)}..."
                    )
                    
                    // Profil verilerini yeniden yükleyelim
                    fetchProfileData()
                } else {
                    // Token yenileme başarısız
                    _tokenTestResult.value = TokenTestResult.Error(
                        "Token yenileme başarısız!\n\n" +
                        "Refresh Token: ${refreshTokenBefore.take(10)}...\n" +
                        "Access Token: ${accessTokenAfter ?: "Alınamadı"}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "TEST - Error during token refresh test", e)
                _tokenTestResult.value = TokenTestResult.Error("Test sırasında hata: ${e.message}")
            }
        }
    }
    
    // Token testi sonuç sınıfları
    sealed class TokenTestResult {
        object Loading : TokenTestResult()
        data class Success(val message: String) : TokenTestResult()
        data class Error(val message: String) : TokenTestResult()
    }
} 