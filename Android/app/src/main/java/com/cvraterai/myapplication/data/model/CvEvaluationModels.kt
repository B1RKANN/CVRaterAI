package com.cvraterai.myapplication.data.model

import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.Date

data class CvEvaluationRequest(
    val userId: Long,
    val fileName: String,
    val fileType: String,
    val githubUrl: String? = null,
    val jobRequirements: String? = null
)

data class CvEvaluationResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileType") val fileType: String,
    @SerializedName("githubUrl") val githubUrl: String?,
    @SerializedName("jobRequirements") val jobRequirements: String?,
    @SerializedName("evaluationScore") val evaluationScore: Int,
    @SerializedName("evaluationResult") val evaluationResult: String,
    @SerializedName("evaluationDate") val evaluationDate: String,
    @SerializedName("fullName") val fullName: String
) {
    // Değerlendirme sonucunu JSON'dan alınan veri olarak işlememizi sağlayan metod
    fun getEvaluationResultJson(): String {
        return evaluationResult
    }
    
    // Değerlendirme puanını yüzdelik değer olarak işlememizi sağlayan metod
    fun getScorePercentage(): Int {
        return evaluationScore
    }
}

// API yanıtında kullanılacak arayüz sınıfları
data class EvaluationResultJson(
    @SerializedName("compatibilityStatus") val compatibilityStatus: Int,
    @SerializedName("userInformation") val userInformation: UserInformation,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("skillRatings") val skillRatings: List<SkillRating>
)

data class UserInformation(
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("skills") val skills: String
)

data class SkillRating(
    @SerializedName("language") val language: String,
    @SerializedName("percentage") val percentage: Int
)

enum class FileType {
    PDF, DOCX, JPG, PNG
} 