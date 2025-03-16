package com.cvraterai.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class EvaluationResponse(
    val id: Long,
    val userId: Long,
    val fileName: String,
    val fileType: String,
    val githubUrl: String?,
    val jobRequirements: String?,
    val evaluationScore: Int,
    val evaluationResult: String,
    val evaluationDate: String,
    val fullName: String
) 