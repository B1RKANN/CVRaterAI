package com.cvraterai.myapplication.ui.cv

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import com.cvraterai.myapplication.data.repository.CvEvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class CvRequiredViewModel @Inject constructor(
    private val cvEvaluationRepository: CvEvaluationRepository
) : ViewModel() {
    private val TAG = "CvRequiredViewModel"
    
    // İşlem adımlarını tanımla
    enum class ProcessStep {
        UPLOADING,    // Dosya yükleniyor
        PARSING,      // CV bilgileri çıkarılıyor
        ANALYZING,    // CV analiz ediliyor
        SCORING,      // Puanlama yapılıyor
        FINALIZING    // Sonuçlar hazırlanıyor
    }
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    
    private val _evaluationResponse = MutableLiveData<CvEvaluationResponse>()
    val evaluationResponse: LiveData<CvEvaluationResponse> = _evaluationResponse
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // İşlem adımını izleyen LiveData
    private val _processStep = MutableLiveData<ProcessStep>()
    val processStep: LiveData<ProcessStep> = _processStep
    
    // Properties passed from UploadCvFragment
    private var selectedFile: File? = null
    
    // Current evaluation job
    private var currentEvaluationJob: Job? = null
    
    fun setSelectedFile(file: File) {
        selectedFile = file
        Log.d(TAG, "Seçilen dosya: ${file.absolutePath}, Boyut: ${file.length()} bytes")
    }
    
    fun evaluateCv(githubUrl: String?, jobRequirements: String?) {
        val currentFile = selectedFile ?: run {
            _error.value = "Dosya seçilmedi"
            return
        }
        
        // Cancel any existing job before starting a new one
        cancelEvaluation()
        
        currentEvaluationJob = viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "CV değerlendirme başlatılıyor - ${currentFile.name}, GitHub URL: $githubUrl, Gereksinimler: $jobRequirements")
                
                // İşlem adımlarını simüle et
                simulateProcessSteps()
                
                val result = cvEvaluationRepository.evaluateCv(
                    file = currentFile,
                    githubUrl = githubUrl,
                    jobRequirements = jobRequirements
                )
                
                // Check if job is still active
                if (!coroutineContext.isActive) {
                    Log.d(TAG, "Değerlendirme iptal edildi")
                    return@launch
                }
                
                // İşlemi tamamla
                _processStep.value = ProcessStep.FINALIZING
                
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
                if (coroutineContext.isActive) {
                    _loading.value = false
                }
            }
        }
    }
    
    /**
     * İşlem adımlarını simüle et
     * Not: Gerçek uygulamada, bu adımlar API işlemlerinin ilerleyişine göre güncellenmelidir
     */
    private suspend fun simulateProcessSteps() = coroutineScope {
        // UPLOADING aşaması
        _processStep.value = ProcessStep.UPLOADING
        delay(1500) // 1.5 saniye bekle
        
        if (!coroutineContext.isActive) return@coroutineScope
        
        // PARSING aşaması
        _processStep.value = ProcessStep.PARSING
        delay(2000) // 2 saniye bekle
        
        if (!coroutineContext.isActive) return@coroutineScope
        
        // ANALYZING aşaması
        _processStep.value = ProcessStep.ANALYZING
        delay(2500) // 2.5 saniye bekle
        
        if (!coroutineContext.isActive) return@coroutineScope
        
        // SCORING aşaması
        _processStep.value = ProcessStep.SCORING
        delay(1000) // 1 saniye bekle
    }
    
    /**
     * İşlemi iptal et
     */
    fun cancelEvaluation() {
        currentEvaluationJob?.let { job ->
            if (job.isActive) {
                Log.d(TAG, "CV değerlendirme işlemi iptal ediliyor")
                job.cancel()
                _loading.value = false
                
                // İptal durumunda error mesajı oluşturmuyoruz
                // Böylece Fragment'ta hata toast'ı gösterilmeyecek
            }
        }
        currentEvaluationJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // ViewModel yok edilirken işlemi iptal et
        cancelEvaluation()
    }
} 