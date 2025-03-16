package com.cvraterai.myapplication.data

import android.util.Base64
import android.util.Log
import org.json.JSONObject

object JwtUtil {
    private const val TAG = "JwtUtil"
    
    fun getUserIdFromToken(token: String): Long? {
        try {
            // JWT tokens consist of three parts: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "Invalid token format")
                return null
            }
            
            // Base64 decode the payload part (second part) of the token
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes)
            
            // Parse the JSON payload
            val jsonObject = JSONObject(decodedPayload)
            
            // The user ID is stored in the "userId" claim
            return if (jsonObject.has("userId")) {
                jsonObject.getLong("userId")
            } else {
                Log.e(TAG, "User ID not found in token")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding token: ${e.message}")
            return null
        }
    }
} 