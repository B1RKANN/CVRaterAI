package com.cvraterai.myapplication.di

import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.api.AuthApiService
import com.cvraterai.myapplication.data.api.CvEvaluationApiService
import com.cvraterai.myapplication.data.api.ProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "http://69.62.120.202:8080/"
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val originalRequest = chain.request()
                var accessToken = tokenManager.getAccessToken()
                
                // İlk istek için token ekle
                val request = if (accessToken != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                } else {
                    originalRequest
                }
                
                // İsteği gönder
                var response = chain.proceed(request)
                
                // 401 hatası alındıysa ve refresh token varsa
                if (response.code == 401 && tokenManager.getRefreshToken() != null) {
                    synchronized(this) {
                        // Token'ı yenilemeyi dene
                        try {
                            val refreshToken = tokenManager.getRefreshToken()
                            if (refreshToken != null) {
                                // Refresh token isteği için yeni bir OkHttpClient oluştur
                                val refreshClient = OkHttpClient.Builder()
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .writeTimeout(30, TimeUnit.SECONDS)
                                    .build()
                                
                                // Refresh token isteği için yeni bir Retrofit instance oluştur
                                val refreshRetrofit = Retrofit.Builder()
                                    .baseUrl(BASE_URL)
                                    .client(refreshClient)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()
                                
                                // Refresh token isteği için yeni bir AuthApiService oluştur
                                val refreshAuthService = refreshRetrofit.create(AuthApiService::class.java)
                                
                                // Suspend fonksiyonu runBlocking ile çağır
                                val refreshResponse = runBlocking {
                                    refreshAuthService.refreshToken(
                                        com.cvraterai.myapplication.data.model.RefreshTokenRequest(refreshToken)
                                    )
                                }
                                
                                if (refreshResponse.isSuccessful) {
                                    refreshResponse.body()?.let { authResponse ->
                                        val newAccessToken = authResponse.getEffectiveAccessToken()
                                        val newRefreshToken = authResponse.getEffectiveRefreshToken()
                                        
                                        if (newAccessToken != null && newRefreshToken != null) {
                                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                                            
                                            // Yeni token ile isteği tekrarla
                                            val newRequest = originalRequest.newBuilder()
                                                .header("Authorization", "Bearer $newAccessToken")
                                                .build()
                                            
                                            response.close()
                                            response = chain.proceed(newRequest)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Refresh token başarısız olursa, kullanıcıyı çıkış yaptır
                            tokenManager.clearTokens()
                        }
                    }
                }
                
                return response
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideCvEvaluationApiService(retrofit: Retrofit): CvEvaluationApiService {
        return retrofit.create(CvEvaluationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }
} 