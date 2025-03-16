package com.cvraterai.myapplication.ui.cv

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import com.cvraterai.myapplication.data.repository.CvEvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CvRequiredViewModel @Inject constructor(
    private val cvEvaluationRepository: CvEvaluationRepository
) : ViewModel() {
    private val TAG = "CvRequiredViewModel"
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _evaluationResponse = MutableLiveData<CvEvaluationResponse>()
    val evaluationResponse: LiveData<CvEvaluationResponse> = _evaluationResponse
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // Properties passed from UploadCvFragment
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
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "CV değerlendirme başlatılıyor - ${currentFile.name}, GitHub URL: $githubUrl, Gereksinimler: $jobRequirements")
                
                val result = cvEvaluationRepository.evaluateCv(
                    file = currentFile,
                    githubUrl = githubUrl,
                    jobRequirements = jobRequirements
                )
                
                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Değerlendirme başarılı: $response")
                        _evaluationResponse.value = response
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Bilinmeyen bir hata oluştu"
                        _error.value = errorMsg
                        Log.e(TAG, "Değerlendirme başarısız: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Bilinmeyen bir hata oluştu"
                _error.value = errorMsg
                Log.e(TAG, "Değerlendirme sırasında hata: $errorMsg", e)
            } finally {
                _loading.value = false
            }
        }
    }
} 