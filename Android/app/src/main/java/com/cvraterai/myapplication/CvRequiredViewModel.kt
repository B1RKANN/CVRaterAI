package com.cvraterai.myapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.model.EvaluationResponse
import com.cvraterai.myapplication.repository.CvEvaluationRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class CvRequiredViewModel : ViewModel() {

    private val repository = CvEvaluationRepository()
    private val TAG = "CvRequiredViewModel"
    
    // LiveData for loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    // LiveData for evaluation result
    private val _evaluationResponse = MutableLiveData<EvaluationResponse?>()
    val evaluationResponse: LiveData<EvaluationResponse?> = _evaluationResponse
    
    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Selected file reference
    private var selectedFile: File? = null
    
    fun setSelectedFile(file: File) {
        selectedFile = file
        Log.d(TAG, "Seçilen dosya: ${file.absolutePath}, Boyut: ${file.length()} bytes")
    }
    
    fun evaluateCv(githubUrl: String?, jobRequirements: String?) {
        val currentFile = selectedFile ?: run {
            _error.value = "Dosya seçilmedi"
            return
        }
        
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "CV değerlendirme başlatılıyor - ${currentFile.name}, GitHub URL: $githubUrl, Gereksinimler: $jobRequirements")
                
                // Dosyayı Uri'ye çevir (gerçek implementasyonda bu farklı olacak)
                val context = currentFile.parentFile?.parentFile // Context almak için uygun bir yöntem bul
                if (context == null) {
                    _error.value = "Dosya işlenirken hata oluştu"
                    _loading.value = false
                    return@launch
                }
                
                // Mock implementasyon - gerçek bir API çağrısı olmadan test amaçlı
                val mockResponse = EvaluationResponse(
                    id = 5,
                    userId = 5,
                    fileName = currentFile.name,
                    fileType = if (currentFile.name.endsWith(".jpg", true)) "JPG" else "PDF",
                    githubUrl = githubUrl,
                    jobRequirements = jobRequirements,
                    evaluationScore = 30,
                    evaluationResult = """{
                        "compatibilityStatus": 30,
                        "userInformation": {
                            "name": "Birkan",
                            "surname": "Boz", 
                            "email": "birkanboz0133@gmail.com",
                            "phone": "05330133011",
                            "skills": "Java, Kotlin, Android, HTML, CSS, JS, Figma"
                        },
                        "explanation": "CV sahibi Firat Universitesi'nde yazilim muhendisligi ogrencisi ve Java, Kotlin, Android, HTML, CSS, JS, Figma gibi teknolojilere hakim. GitHub profilinde Kotlin agirlikli calismalar bulunuyor. Ancak, istenen deneyim ve framework bilgisi acisindan gereksinimleri tam olarak karsilamiyor.",
                        "skillRatings": [
                            {"language": "Kotlin", "percentage": 100}
                        ]
                    }""",
                    evaluationDate = "2025-03-16T17:30:42.870+00:00",
                    fullName = "Birkan Boz"
                )
                
                _evaluationResponse.postValue(mockResponse)
                Log.d(TAG, "CV değerlendirme tamamlandı: $mockResponse")
                
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Bilinmeyen bir hata oluştu"
                _error.postValue(errorMsg)
                Log.e(TAG, "CV değerlendirme hatası: $errorMsg", e)
            } finally {
                _loading.postValue(false)
            }
        }
    }
} 