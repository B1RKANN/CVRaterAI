package com.cvraterai.myapplication.data

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val TAG = "TokenManager"
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveTokens(accessToken: String?, refreshToken: String?) {
        Log.d(TAG, "Saving tokens - Access Token: $accessToken, Refresh Token: $refreshToken")
        println("Saving tokens - Access Token: $accessToken, Refresh Token: $refreshToken")
        
        try {
            val editor = sharedPreferences.edit()
            
            // Refresh token her zaman kaydedilmeli
            if (refreshToken != null) {
                editor.putString(REFRESH_TOKEN_KEY, refreshToken)
                Log.d(TAG, "Refresh token saved")
                println("Refresh token saved")
            }
            
            // Access token null olabilir, bu durumda kaydetme
            if (accessToken != null) {
                editor.putString(ACCESS_TOKEN_KEY, accessToken)
                Log.d(TAG, "Access token saved")
                println("Access token saved")
            }
            
            // Değişiklikleri uygula
            val result = editor.commit() // apply() yerine commit() kullanarak işlemin tamamlanmasını bekleyelim
            Log.d(TAG, "Save result: $result")
            println("Save result: $result")
            
            // Kaydedilen değerleri kontrol et
            val savedAccessToken = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
            val savedRefreshToken = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
            Log.d(TAG, "Verification - Saved Access Token: $savedAccessToken")
            Log.d(TAG, "Verification - Saved Refresh Token: $savedRefreshToken")
            println("Verification - Saved Access Token: $savedAccessToken")
            println("Verification - Saved Refresh Token: $savedRefreshToken")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tokens", e)
            println("Error saving tokens: ${e.message}")
        }
    }
    
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
        Log.d(TAG, "Getting access token: $token")
        println("Getting access token: $token")
        return token
    }
    
    fun getRefreshToken(): String? {
        val token = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
        Log.d(TAG, "Getting refresh token: $token")
        println("Getting refresh token: $token")
        return token
    }
    
    fun clearTokens() {
        Log.d(TAG, "Clearing tokens")
        println("Clearing tokens")
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
    }
    
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
} 