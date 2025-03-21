package com.cvraterai.myapplication.data.repository

import android.util.Log
import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.api.AuthApiService
import com.cvraterai.myapplication.data.model.AuthResponse
import com.cvraterai.myapplication.data.model.LoginRequest
import com.cvraterai.myapplication.data.model.RefreshTokenRequest
import com.cvraterai.myapplication.data.model.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    
    private val TAG = "AuthRepository"
    
    suspend fun register(name: String, email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(
                    RegisterRequest(name, email, password)
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Yeni yardımcı metodları kullanalım
                        val accessToken = authResponse.getEffectiveAccessToken()
                        val refreshToken = authResponse.getEffectiveRefreshToken()
                        
                        Log.d(TAG, "Register - Effective Access Token: $accessToken")
                        Log.d(TAG, "Register - Effective Refresh Token: $refreshToken")
                        println("Register - Effective Access Token: $accessToken")
                        println("Register - Effective Refresh Token: $refreshToken")
                        
                        // Token'ları saklayalım
                        if (refreshToken != null) {
                            tokenManager.saveTokens(accessToken, refreshToken)
                        }
                        
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "{}").optString("message", "Kayıt işlemi başarısız")
                    } catch (e: Exception) {
                        "Kayıt işlemi başarısız"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register error", e)
                println("Register error: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(
                    LoginRequest(email, password)
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Debug log ekleyelim
                        Log.d(TAG, "Raw API Response - Access Token: ${authResponse.accessToken}")
                        Log.d(TAG, "Raw API Response - Refresh Token: ${authResponse.refreshToken}")
                        Log.d(TAG, "Raw API Response - Token: ${authResponse.token}")
                        Log.d(TAG, "Raw API Response - Access Token (alt): ${authResponse.access_token}")
                        Log.d(TAG, "Raw API Response - Refresh Token (alt): ${authResponse.refresh_token}")
                        Log.d(TAG, "Raw API Response - ID Token: ${authResponse.idToken}")
                        
                        println("Raw API Response - Access Token: ${authResponse.accessToken}")
                        println("Raw API Response - Refresh Token: ${authResponse.refreshToken}")
                        println("Raw API Response - Token: ${authResponse.token}")
                        println("Raw API Response - Access Token (alt): ${authResponse.access_token}")
                        println("Raw API Response - Refresh Token (alt): ${authResponse.refresh_token}")
                        println("Raw API Response - ID Token: ${authResponse.idToken}")
                        
                        // Backend'den dönen yanıtın tamamını görelim
                        Log.d(TAG, "Full Auth Response: $authResponse")
                        println("Full Auth Response: $authResponse")
                        
                        // Yeni yardımcı metodları kullanalım
                        val accessToken = authResponse.getEffectiveAccessToken()
                        val refreshToken = authResponse.getEffectiveRefreshToken()
                        
                        Log.d(TAG, "Login - Effective Access Token: $accessToken")
                        Log.d(TAG, "Login - Effective Refresh Token: $refreshToken")
                        println("Login - Effective Access Token: $accessToken")
                        println("Login - Effective Refresh Token: $refreshToken")
                        
                        // Token'ları saklayalım
                        if (refreshToken != null) {
                            tokenManager.saveTokens(accessToken, refreshToken)
                            
                            // Token'ların saklanıp saklanmadığını kontrol edelim
                            Log.d(TAG, "After Save - Access Token: ${tokenManager.getAccessToken()}")
                            Log.d(TAG, "After Save - Refresh Token: ${tokenManager.getRefreshToken()}")
                            println("After Save - Access Token: ${tokenManager.getAccessToken()}")
                            println("After Save - Refresh Token: ${tokenManager.getRefreshToken()}")
                        } else {
                            Log.e(TAG, "Refresh token is null, cannot save tokens")
                            println("Refresh token is null, cannot save tokens")
                        }
                        
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val jsonObject = JSONObject(errorBody ?: "{}")
                        val message = jsonObject.optString("message", "")
                        
                        when {
                            message.contains("email") || message.contains("e-mail") || message.contains("kullanıcı") || 
                            message.contains("user") || message.contains("not found") || message.contains("bulunamadı") -> 
                                "E-posta adresi kayıtlı değil"
                            
                            message.contains("password") || message.contains("şifre") || message.contains("parola") || 
                            message.contains("incorrect") || message.contains("yanlış") -> 
                                "Yanlış şifre girdiniz"
                            
                            else -> message.ifEmpty { "Giriş başarısız" }
                        }
                    } catch (e: Exception) {
                        "Giriş başarısız"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                println("Login error: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun refreshToken(): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                    ?: return@withContext Result.failure(Exception("No refresh token available"))
                
                Log.d(TAG, "Attempting to refresh token with: $refreshToken")
                
                val response = authApiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )
                
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // Yeni yardımcı metodları kullanalım
                        val accessToken = authResponse.getEffectiveAccessToken()
                        val newRefreshToken = authResponse.getEffectiveRefreshToken()
                        
                        Log.d(TAG, "Refresh Token - Effective Access Token: $accessToken")
                        Log.d(TAG, "Refresh Token - Effective Refresh Token: $newRefreshToken")
                        
                        // Token'ları saklayalım
                        if (newRefreshToken != null) {
                            tokenManager.saveTokens(accessToken, newRefreshToken)
                        } else if (accessToken != null) {
                            // Eğer yeni refresh token yoksa ama access token varsa, mevcut refresh token ile birlikte saklayalım
                            tokenManager.saveTokens(accessToken, refreshToken)
                        }
                        
                        return@withContext Result.success(authResponse)
                    } ?: return@withContext Result.failure(Exception("Empty response body"))
                } else {
                    // Refresh token geçersiz olabilir, kullanıcıyı çıkış yaptıralım
                    if (response.code() == 401) {
                        tokenManager.clearTokens()
                    }
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        JSONObject(errorBody ?: "{}").optString("message", "Token yenileme başarısız")
                    } catch (e: Exception) {
                        "Token yenileme başarısız"
                    }
                    return@withContext Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh token error", e)
                return@withContext Result.failure(e)
            }
        }
    }
    
    fun logout() {
        tokenManager.clearTokens()
    }
    
    fun isLoggedIn(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        
        Log.d(TAG, "isLoggedIn - Access Token: $accessToken")
        Log.d(TAG, "isLoggedIn - Refresh Token: $refreshToken")
        println("isLoggedIn - Access Token: $accessToken")
        println("isLoggedIn - Refresh Token: $refreshToken")
        
        // Eğer refresh token varsa ve access token yoksa, refresh token işlemini başlat
        if (refreshToken != null && accessToken == null) {
            Log.d(TAG, "Access token is null but refresh token exists, initiating refresh")
            // Coroutine scope'da refresh token işlemini başlat
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val result = refreshToken()
                    if (result.isSuccess) {
                        Log.d(TAG, "Token refresh successful")
                    } else {
                        Log.e(TAG, "Token refresh failed: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Token refresh error", e)
                }
            }
        }
        
        // Eğer refresh token varsa, kullanıcı giriş yapmış sayılır
        // Access token yoksa bile refresh token ile yeni bir access token alınabilir
        return refreshToken != null
    }
    
    // TokenManager'dan token'ları almak için metodlar
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
    
    fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }
} 