package com.cvraterai.myapplication.data.api

import com.cvraterai.myapplication.data.model.AuthResponse
import com.cvraterai.myapplication.data.model.LoginRequest
import com.cvraterai.myapplication.data.model.RefreshTokenRequest
import com.cvraterai.myapplication.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/v2/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/v2/authenticate")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    @POST("auth/v2/refreshToken")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<AuthResponse>
    
    @POST("auth/v2/refreshToken")
    fun refreshTokenSync(@Body refreshTokenRequest: RefreshTokenRequest): retrofit2.Call<AuthResponse>
} 