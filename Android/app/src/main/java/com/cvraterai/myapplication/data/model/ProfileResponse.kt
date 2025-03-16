package com.cvraterai.myapplication.data.model

data class ProfileResponse(
    val id: Long,
    val name: String,
    val email: String,
    val userCredit: Int,
    val planType: String
) 