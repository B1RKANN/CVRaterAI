package com.cvraterai.myapplication.data.api

import com.cvraterai.myapplication.data.model.ApiResponse
import com.cvraterai.myapplication.data.model.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApiService {
    @GET("api/v1/profile/{id}")
    suspend fun getProfile(@Path("id") id: Long): Response<ApiResponse<ProfileResponse>>
} 