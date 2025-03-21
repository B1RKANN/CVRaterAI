package com.cvraterai.myapplication.di

import com.cvraterai.myapplication.data.TokenManager
import com.cvraterai.myapplication.data.api.AuthApiService
import com.cvraterai.myapplication.data.api.CvEvaluationApiService
import com.cvraterai.myapplication.data.api.ProfileApiService
import com.cvraterai.myapplication.data.model.RefreshTokenRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "http://69.62.120.202:8080/"
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            
            // Eğer endpoint auth ile ilgiliyse (login, register, refresh-token), token eklemeye gerek yok
            if (request.url.encodedPath.contains("/auth/")) {
                Log.d("NetworkModule", "Auth endpoint, skipping token: ${request.url.encodedPath}")
                return@Interceptor chain.proceed(request)
            }
            
            val accessToken = tokenManager.getAccessToken()
            Log.d("NetworkModule", "Access token in interceptor: $accessToken")
            
            val authenticatedRequest = if (accessToken != null) {
                Log.d("NetworkModule", "Using existing access token")
                request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                Log.d("NetworkModule", "No access token, adding dummy token to trigger 401")
                // Access token yoksa dummy token ekle, Authenticator 401'i yakalayacak
                request.newBuilder()
                    .header("Authorization", "Bearer dummy-token")
                    .build()
            }
            
            Log.d("NetworkModule", "Proceeding with authenticated request: ${authenticatedRequest.url}")
            chain.proceed(authenticatedRequest)
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @AuthOkHttpClient
    @Provides
    @Singleton
    fun provideAuthOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @AuthRetrofit
    @Provides
    @Singleton
    fun provideAuthRetrofit(
        @AuthOkHttpClient authOkHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@AuthRetrofit authRetrofit: Retrofit): AuthApiService {
        return authRetrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideCvEvaluationApiService(@MainOkHttpClient mainOkHttpClient: OkHttpClient): CvEvaluationApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mainOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(CvEvaluationApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideProfileApiService(@MainOkHttpClient mainOkHttpClient: OkHttpClient): ProfileApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mainOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ProfileApiService::class.java)
    }

    @MainOkHttpClient
    @Provides
    @Singleton
    fun provideMainOkHttpClient(
        authInterceptor: Interceptor,
        authenticator: Authenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Önce logging
            .addInterceptor { chain -> // Sonra request logging
                Log.d("NetworkModule", "Starting request: ${chain.request().url}")
                chain.proceed(chain.request())
            }
            .addInterceptor(authInterceptor) // En son auth interceptor
            .authenticator(authenticator)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMainRetrofit(
        @MainOkHttpClient mainOkHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mainOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        tokenManager: TokenManager,
        @AuthRetrofit authRetrofit: Retrofit
    ): Authenticator {
        val authApiService = authRetrofit.create(AuthApiService::class.java)
        
        return Authenticator { route, response ->
            Log.d("NetworkModule", "╔═══════════════════════════════════════════")
            Log.d("NetworkModule", "║ AUTHENTICATOR START")
            Log.d("NetworkModule", "║ Original request URL: ${response.request.url}")
            Log.d("NetworkModule", "║ Response code: ${response.code}")
            Log.d("NetworkModule", "║ Response message: ${response.message}")
            Log.d("NetworkModule", "║ Response headers: ${response.headers}")
            Log.d("NetworkModule", "║ Original request headers: ${response.request.headers}")
            
            // Read response body to check for "Access Denied" message
            var responseBody: String? 
            try {
                // Response body sadece bir kez okunabilir, bir kopya almalıyız
                val originalBody = response.peekBody(Long.MAX_VALUE).string()
                responseBody = originalBody
                Log.d("NetworkModule", "║ Response body: $responseBody")
            } catch (e: Exception) {
                Log.e("NetworkModule", "║ Error reading response body: ${e.message}")
                responseBody = null
            }
            
            // Check if this is an auth-related error (either 401 or 500 with "Access Denied")
            val isAuthError = response.code == 401 || 
                (response.code == 500 && responseBody?.contains("Access Denied") == true)
            
            if (!isAuthError) {
                Log.d("NetworkModule", "║ Not an auth error, skipping token refresh")
                Log.d("NetworkModule", "║ AUTHENTICATOR END (NOT AUTH ERROR)")
                Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                return@Authenticator null
            }
            
            val refreshToken = tokenManager.getRefreshToken()
            Log.d("NetworkModule", "║ Current refresh token: ${refreshToken?.take(10) ?: "null"}...")
            
            if (refreshToken == null) {
                Log.d("NetworkModule", "║ No refresh token available")
                Log.d("NetworkModule", "║ AUTHENTICATOR END (NO REFRESH TOKEN)")
                Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                return@Authenticator null
            }

            // Eğer refresh token isteği başarısız olduysa, null dön
            if (response.request.url.encodedPath.contains("/auth/v2/refreshToken")) {
                Log.d("NetworkModule", "║ Preventing refresh token request loop")
                Log.d("NetworkModule", "║ AUTHENTICATOR END (PREVENTING LOOP)")
                Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                return@Authenticator null
            }

            // Senkron bir şekilde refresh token isteği yap
            runBlocking {
                try {
                    Log.d("NetworkModule", "║ Making refresh token request...")
                    val request = RefreshTokenRequest(refreshToken)
                    Log.d("NetworkModule", "║ Refresh token request body: $request")
                    Log.d("NetworkModule", "║ Refresh token request body raw: refresh_token=${refreshToken.take(10)}...")
                    
                    val refreshTokenResponse = authApiService.refreshTokenSync(request).execute()
                    Log.d("NetworkModule", "║ Refresh token response code: ${refreshTokenResponse.code()}")
                    Log.d("NetworkModule", "║ Refresh token response message: ${refreshTokenResponse.message()}")
                    Log.d("NetworkModule", "║ Refresh token response headers: ${refreshTokenResponse.headers()}")
                    
                    if (refreshTokenResponse.isSuccessful) {
                        val body = refreshTokenResponse.body()
                        Log.d("NetworkModule", "║ Refresh token response body: $body")
                        
                        if (body != null) {
                            val newAccessToken = body.getEffectiveAccessToken()
                            val newRefreshToken = body.getEffectiveRefreshToken()
                            
                            // API yanıtını daha detaylı logla
                            Log.d("NetworkModule", "║ Raw access token: ${body.accessToken}")
                            Log.d("NetworkModule", "║ Raw refresh token: ${body.refreshToken}")
                            Log.d("NetworkModule", "║ Raw token: ${body.token}")
                            Log.d("NetworkModule", "║ Raw access_token: ${body.access_token}")
                            Log.d("NetworkModule", "║ Raw refresh_token: ${body.refresh_token}")
                            
                            Log.d("NetworkModule", "║ New access token: ${newAccessToken?.take(20) ?: "null"}...")
                            Log.d("NetworkModule", "║ New refresh token: ${newRefreshToken?.take(10) ?: "null"}...")
                            
                            if (newAccessToken != null && newRefreshToken != null) {
                                tokenManager.saveTokens(newAccessToken, newRefreshToken)
                                Log.d("NetworkModule", "║ Successfully saved new tokens")
                                
                                // Yeni token ile orijinal isteği tekrarla
                                val newRequest = response.request.newBuilder()
                                    .removeHeader("Authorization")
                                    .addHeader("Authorization", "Bearer $newAccessToken")
                                    .build()
                                Log.d("NetworkModule", "║ Created new request with updated token")
                                Log.d("NetworkModule", "║ New request headers: ${newRequest.headers}")
                                Log.d("NetworkModule", "║ AUTHENTICATOR END (SUCCESS)")
                                Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                                return@runBlocking newRequest
                            } else {
                                Log.e("NetworkModule", "║ ERROR: New tokens are null after successful response")
                            }
                        } else {
                            Log.e("NetworkModule", "║ ERROR: Response body is null after successful response")
                        }
                    } else {
                        val errorBody = try {
                            refreshTokenResponse.errorBody()?.string() ?: "No error body"
                        } catch (e: Exception) {
                            "Error reading error body: ${e.message}"
                        }
                        
                        Log.e("NetworkModule", "║ ERROR: Token refresh failed")
                        Log.e("NetworkModule", "║ Response Code: ${refreshTokenResponse.code()}")
                        Log.e("NetworkModule", "║ Request URL: ${refreshTokenResponse.raw().request.url}")
                        Log.e("NetworkModule", "║ Request Headers: ${refreshTokenResponse.raw().request.headers}")
                        Log.e("NetworkModule", "║ Error Body: $errorBody")
                    }
                    Log.d("NetworkModule", "║ AUTHENTICATOR END (FAILED)")
                    Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                    null
                } catch (e: Exception) {
                    Log.e("NetworkModule", "║ ERROR: Exception during token refresh", e)
                    Log.e("NetworkModule", "║ Exception message: ${e.message}")
                    Log.e("NetworkModule", "║ Stack trace: ${e.stackTraceToString()}")
                    Log.d("NetworkModule", "║ AUTHENTICATOR END (ERROR)")
                    Log.d("NetworkModule", "╚═══════════════════════════════════════════")
                    null
                }
            }
        }
    }
} 