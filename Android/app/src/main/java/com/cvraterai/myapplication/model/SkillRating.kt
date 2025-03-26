package com.cvraterai.myapplication.model

import com.google.gson.annotations.SerializedName

data class SkillRating(
    @SerializedName("language") val name: String,
    @SerializedName("percentage") val percentage: Int
) 