package com.cvraterai.myapplication.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cvraterai.myapplication.model.EvaluationResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class CvEvaluationRepository {
    private val TAG = "CvEvaluationRepository"
    private val BASE_URL = "https://api.cvrater.ai/api/v1"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun validateToken(context: Context): String? = withContext(Dispatchers.IO) {
        // Bu fonksiyon normalde TokenManager veya AuthRepository'den token'ı alıp 
        // geçerliliğini kontrol edecektir
        // Şimdilik dummy bir token döndürelim
        return@withContext "dummy_token"
    }
    
    suspend fun uploadCvFile(
        context: Context,
        fileUri: Uri,
        fileName: String,
        token: String
    ): EvaluationResponse? = withContext(Dispatchers.IO) {
        try {
            // Uri'den geçici bir dosya oluştur
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val tempFile = File(context.cacheDir, fileName)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // MultipartBody oluştur
            val requestBody = tempFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
            
            // API çağrısını manuel olarak yap
            // Gerçek uygulamada burada Retrofit kullanılabilir
            // Şimdilik dummy bir yanıt döndürelim
            val dummyResponse = EvaluationResponse(
                id = 5,
                userId = 5,
                fileName = "Birkan Boz CV.jpg",
                fileType = "JPG",
                githubUrl = "https://github.com/B1RKANN",
                jobRequirements = "2 yıl deneyim...",
                evaluationScore = 75,
                evaluationResult = """{
                    "compatibilityStatus": 75,
                    "userInformation": {
                        "name": "Birkan",
                        "surname": "Boz",
                        "email": "birkanboz0133@gmail.com",
                        "phone": "0533 013 3011",
                        "skills": "Java, Kotlin, Android, HTML, CSS, JS, Figma"
                    },
                    "explanation": "CV sahibi yazılım mühendisliği öğrencisi ve birçok alanda deneyim sahibi.",
                    "skillRatings": [
                        {"language": "Kotlin", "percentage": 90},
                        {"language": "Java", "percentage": 60},
                        {"language": "HTML", "percentage": 75},
                        {"language": "CSS", "percentage": 70},
                        {"language": "JavaScript", "percentage": 65}
                    ]
                }""",
                evaluationDate = "2025-03-16T17:30:42.870+00:00",
                fullName = "Birkan Boz"
            )
            
            return@withContext dummyResponse
            
        } catch (e: IOException) {
            Log.e(TAG, "Error uploading file: ${e.message}", e)
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            return@withContext null
        }
    }
} 