package com.cvraterai.myapplication.data.model

import com.google.gson.annotations.SerializedName

// İstek modelleri
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

// Yanıt modelleri
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("token") val token: String? = null, // Bazı API'ler "token" olarak döndürebilir
    @SerializedName("refresh_token") val refresh_token: String? = null, // Bazı API'ler "refresh_token" olarak döndürebilir
    @SerializedName("access_token") val access_token: String? = null, // Bazı API'ler "access_token" olarak döndürebilir
    @SerializedName("user") val user: User? = null,
    @SerializedName("id_token") val idToken: String? = null // Bazı API'ler "id_token" olarak döndürebilir
) {
    // Gerçek access token'ı almak için yardımcı metod
    fun getEffectiveAccessToken(): String? {
        return accessToken ?: access_token ?: token ?: idToken
    }
    
    // Gerçek refresh token'ı almak için yardımcı metod
    fun getEffectiveRefreshToken(): String? {
        return refreshToken ?: refresh_token
    }
}

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
) 