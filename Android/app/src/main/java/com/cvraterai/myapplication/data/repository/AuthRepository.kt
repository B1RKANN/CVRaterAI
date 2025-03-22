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
import kotlinx.coroutines.runBlocking
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
        
        // Refresh token yoksa kullanıcı giriş yapmamış demektir
        if (refreshToken == null) {
            return false
        }
        
        // Eğer refresh token varsa ve access token yoksa, senkron olarak refresh token işlemini başlat
        if (accessToken == null) {
            Log.d(TAG, "Access token is null but refresh token exists, initiating refresh synchronously")
            // Senkron olarak refresh token işlemini gerçekleştir
            val result = ensureValidAccessTokenSync()
            
            // Yenileme başarılı olduysa true döndür, değilse (refresh token geçersiz olabilir) false döndür
            if (!result) {
                Log.e(TAG, "Token refresh failed during isLoggedIn check")
                return false
            }
        }
        
        // Bu noktada ya geçerli bir access token vardı ya da refresh token ile yeni bir token alındı
        return true
    }
    
    // Senkron olarak erişim belirtecinin geçerli olduğunu garantileme
    fun ensureValidAccessTokenSync(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        val refreshToken = tokenManager.getRefreshToken()
        
        // Eğer zaten geçerli bir access token varsa, true döndür
        if (accessToken != null) {
            Log.d(TAG, "Access token already valid")
            return true
        }
        
        // Eğer refresh token yoksa, false döndür
        if (refreshToken == null) {
            Log.e(TAG, "No refresh token available")
            return false
        }
        
        // Access token yoksa, refresh token kullanarak yeni bir access token al
        return runBlocking(Dispatchers.IO) {
            try {
                Log.d(TAG, "Synchronously refreshing access token")
                val request = RefreshTokenRequest(refreshToken)
                val response = authApiService.refreshTokenSync(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val newAccessToken = body.getEffectiveAccessToken()
                        val newRefreshToken = body.getEffectiveRefreshToken()
                        
                        if (newAccessToken != null && newRefreshToken != null) {
                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            Log.d(TAG, "Successfully refreshed tokens synchronously")
                            return@runBlocking true
                        } else {
                            Log.e(TAG, "Refreshed tokens are null after successful response")
                        }
                    } else {
                        Log.e(TAG, "Response body is null after successful response")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Token refresh failed: ${response.code()}, Error: $errorBody")
                    
                    // 401 veya 500 hata kodu gelirse ve "token is not in database" mesajı varsa
                    // refresh token geçersiz demektir, token'ları temizle
                    val errorMessage = errorBody ?: ""
                    if (response.code() == 401 || (response.code() == 500 && errorMessage.contains("token is not in database", true))) {
                        Log.d(TAG, "Clearing tokens due to invalid refresh token")
                        tokenManager.clearTokens()
                    }
                }
                
                false
            } catch (e: Exception) {
                Log.e(TAG, "Exception during token refresh", e)
                false
            }
        }
    }
    
    // TokenManager'dan token'ları almak için metodlar
    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
    
    fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }
} 