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
        
        try {
            val editor = sharedPreferences.edit()
            
            // Refresh token her zaman kaydedilmeli
            if (refreshToken != null) {
                editor.putString(REFRESH_TOKEN_KEY, refreshToken)
                Log.d(TAG, "Refresh token saved")
            }
            
            // Access token null olabilir, bu durumda kaydetme
            if (accessToken != null) {
                editor.putString(ACCESS_TOKEN_KEY, accessToken)
                // Access token'ın son geçerlilik zamanını kaydet
                val expirationTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 saat
                editor.putLong(ACCESS_TOKEN_EXPIRATION_KEY, expirationTime)
                Log.d(TAG, "Access token saved with expiration: $expirationTime")
            }
            
            // Değişiklikleri uygula
            val result = editor.commit()
            Log.d(TAG, "Save result: $result")
            
            // Kaydedilen değerleri kontrol et
            val savedAccessToken = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
            val savedRefreshToken = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
            Log.d(TAG, "Verification - Saved Access Token: $savedAccessToken")
            Log.d(TAG, "Verification - Saved Refresh Token: $savedRefreshToken")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tokens", e)
        }
    }
    
    fun getAccessToken(): String? {
        val token = sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
        val expirationTime = sharedPreferences.getLong(ACCESS_TOKEN_EXPIRATION_KEY, 0)
        
        // Token'ın süresi dolmuşsa null döndür
        if (token != null && System.currentTimeMillis() > expirationTime) {
            Log.d(TAG, "Access token expired")
            // Access token'ı sil
            sharedPreferences.edit()
                .remove(ACCESS_TOKEN_KEY)
                .remove(ACCESS_TOKEN_EXPIRATION_KEY)
                .apply()
            return null
        }
        
        Log.d(TAG, "Getting access token: $token")
        return token
    }
    
    fun getRefreshToken(): String? {
        val token = sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
        Log.d(TAG, "Getting refresh token: $token")
        return token
    }
    
    fun clearTokens() {
        Log.d(TAG, "Clearing tokens")
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .remove(ACCESS_TOKEN_EXPIRATION_KEY)
            .apply()
    }
    
    fun clearAccessToken() {
        Log.d(TAG, "Clearing access token")
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(ACCESS_TOKEN_EXPIRATION_KEY)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        val refreshToken = getRefreshToken()
        Log.d(TAG, "isLoggedIn - Refresh Token: $refreshToken")
        return refreshToken != null
    }
    
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val ACCESS_TOKEN_EXPIRATION_KEY = "access_token_expiration"
    }
} 