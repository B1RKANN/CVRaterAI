package com.cvraterai.myapplication.data.repository

import android.util.Log
import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.JwtUtil
import com.cvraterai.myapplication.data.api.CvEvaluationApiService
import com.cvraterai.myapplication.data.model.ApiResponse
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import com.cvraterai.myapplication.data.model.FileType
import com.cvraterai.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvEvaluationRepository @Inject constructor(
    private val cvEvaluationApiService: CvEvaluationApiService,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {
    private val TAG = "CvEvaluationRepository"
    
    private suspend fun ensureValidToken(): Result<String> = withContext(Dispatchers.IO) {
        val accessToken = tokenManager.getAccessToken()
        
        if (accessToken.isNullOrEmpty()) {
            // Access token yoksa, refresh token ile yeni token almayı deneyelim
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                Log.e(TAG, "No tokens available")
                return@withContext Result.failure(Exception("Oturum bilgisi bulunamadı. Lütfen tekrar giriş yapın."))
            }
            
            Log.d(TAG, "Access token empty, trying to refresh token")
            val refreshResult = authRepository.refreshToken()
            
            if (refreshResult.isSuccess) {
                val newAccessToken = tokenManager.getAccessToken()
                if (newAccessToken.isNullOrEmpty()) {
                    Log.e(TAG, "Refreshed but access token is still null")
                    return@withContext Result.failure(Exception("Token yenilemesi başarısız. Lütfen tekrar giriş yapın."))
                }
                
                Log.d(TAG, "Token refreshed successfully")
                return@withContext Result.success("Bearer $newAccessToken")
            } else {
                Log.e(TAG, "Token refresh failed: ${refreshResult.exceptionOrNull()?.message}")
                return@withContext Result.failure(Exception("Oturum süresi dolmuş. Lütfen tekrar giriş yapın."))
            }
        } else {
            Log.d(TAG, "Using existing access token")
            return@withContext Result.success("Bearer $accessToken")
        }
    }
    
    suspend fun evaluateCv(
        file: File,
        githubUrl: String? = null,
        jobRequirements: String? = null
    ): Result<CvEvaluationResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting CV evaluation for file: ${file.name}")
            
            // Token kontrolü
            val authHeaderResult = ensureValidToken()
            if (authHeaderResult.isFailure) {
                return@withContext Result.failure(authHeaderResult.exceptionOrNull()!!)
            }
            
            val authHeader = authHeaderResult.getOrThrow()
            
            // Get user ID from token
            val accessToken = tokenManager.getAccessToken() ?: return@withContext Result.failure(Exception("Oturum bilgisi bulunamadı. Lütfen tekrar giriş yapın."))
            val userId = JwtUtil.getUserIdFromToken(accessToken) ?: return@withContext Result.failure(Exception("Kullanıcı ID'si alınamadı. Lütfen tekrar giriş yapın."))
            
            // Create file part
            val fileRequestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val fileMultipartBody = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)
            
            // Log file info
            Log.d(TAG, "File to evaluate: ${file.absolutePath}, Size: ${file.length()} bytes, Exists: ${file.exists()}")
            
            // Create optional parts
            val githubUrlRequestBody = githubUrl?.let {
                Log.d(TAG, "GitHub URL: $it")
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            
            val jobRequirementsRequestBody = jobRequirements?.let {
                Log.d(TAG, "Job Requirements: $it")
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }
            
            // Make API call
            Log.d(TAG, "Making CV evaluation API call with userId: $userId")
            val response = cvEvaluationApiService.evaluateCv(
                userId = userId,
                file = fileMultipartBody,
                githubUrl = githubUrlRequestBody,
                jobRequirements = jobRequirementsRequestBody,
                authorization = authHeader
            )
            
            Log.d(TAG, "Evaluation API response: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                
                if (responseBody != null) {
                    Log.d(TAG, "CV evaluation successful with response: $responseBody")
                    return@withContext Result.success(responseBody)
                } else {
                    val errorMsg = "Değerlendirme yanıtı boş"
                    Log.e(TAG, errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorCode = response.code()
                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    "HTTP $errorCode: $errorBody"
                } else {
                    "HTTP $errorCode: Unknown error"
                }
                
                if (errorCode == 401) {
                    // 401 hatası aldık, token'ı yenilemeyi deneyelim
                    Log.e(TAG, "Authentication error (401), trying to refresh token")
                    val refreshResult = authRepository.refreshToken()
                    
                    if (refreshResult.isSuccess) {
                        Log.d(TAG, "Token refreshed, retrying evaluation")
                        // Token yenilendi, işlemi tekrar deneyelim
                        return@withContext evaluateCv(file, githubUrl, jobRequirements)
                    } else {
                        Log.e(TAG, "Token refresh failed after 401: ${refreshResult.exceptionOrNull()?.message}")
                        return@withContext Result.failure(Exception("Oturum süreniz dolmuş. Lütfen tekrar giriş yapın."))
                    }
                }
                
                Log.e(TAG, "Error evaluating CV: $errorMessage")
                return@withContext Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during CV evaluation: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun getUserEvaluations(userId: Long): Result<List<CvEvaluationResponse>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken() ?: return@withContext Result.failure(Exception("Token bulunamadı"))
                val response = cvEvaluationApiService.getUserEvaluations(userId, "Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val evaluations = response.body() ?: emptyList()
                    Result.success(evaluations)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                    Log.e("CvEvaluationRepository", "getUserEvaluations Hata: $errorBody")
                    Result.failure(Exception(errorBody))
                }
            } catch (e: Exception) {
                Log.e("CvEvaluationRepository", "getUserEvaluations Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun getEvaluationById(evaluationId: Long): Result<CvEvaluationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getAccessToken() ?: return@withContext Result.failure(Exception("Token bulunamadı"))
                val response = cvEvaluationApiService.getEvaluationById(evaluationId, "Bearer $token")
                
                if (response.isSuccessful && response.body() != null) {
                    val evaluation = response.body()!!
                    Result.success(evaluation)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Bilinmeyen hata"
                    Log.e("CvEvaluationRepository", "getEvaluationById Hata: $errorBody")
                    Result.failure(Exception(errorBody))
                }
            } catch (e: Exception) {
                Log.e("CvEvaluationRepository", "getEvaluationById Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }
} 