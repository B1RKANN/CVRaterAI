package com.cvraterai.myapplication.data.model

data class ApiResponse<T>(
    val status: Int,
    val payload: T,
    val errorMessage: String?
) 