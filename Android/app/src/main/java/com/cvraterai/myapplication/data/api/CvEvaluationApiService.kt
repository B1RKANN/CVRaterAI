package com.cvraterai.myapplication.data.api

import com.cvraterai.myapplication.data.model.ApiResponse
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface CvEvaluationApiService {
    @Multipart
    @POST("api/v1/cv-evaluation/upload")
    suspend fun uploadCv(
        @Part file: MultipartBody.Part,
        @Part("fileType") fileType: RequestBody,
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<String>> // Returns file ID or path
    
    @Multipart
    @POST("api/v1/cv-evaluation/evaluate/{userId}")
    suspend fun evaluateCv(
        @Path("userId") userId: Long,
        @Part file: MultipartBody.Part,
        @Part("githubUrl") githubUrl: RequestBody?,
        @Part("jobRequirements") jobRequirements: RequestBody?,
        @Header("Authorization") authorization: String
    ): Response<CvEvaluationResponse>
    
    @GET("api/v1/cv-evaluation/user/{userId}")
    suspend fun getUserEvaluations(
        @Path("userId") userId: Long,
        @Header("Authorization") authorization: String
    ): Response<List<CvEvaluationResponse>>
    
    @GET("api/v1/cv-evaluation/evaluate/{id}")
    suspend fun getEvaluationById(
        @Path("id") id: Long,
        @Header("Authorization") authorization: String
    ): Response<CvEvaluationResponse>
} 