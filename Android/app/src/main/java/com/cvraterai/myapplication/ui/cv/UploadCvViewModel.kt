package com.cvraterai.myapplication.ui.cv

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cvraterai.myapplication.data.model.FileType
import com.cvraterai.myapplication.data.repository.CvEvaluationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadCvViewModel @Inject constructor() : ViewModel() {
    private val TAG = "UploadCvViewModel"
    
    private val _fileSelected = MutableLiveData<Boolean>()
    val fileSelected: LiveData<Boolean> = _fileSelected
    
    private val _selectedFileName = MutableLiveData<String>()
    val selectedFileName: LiveData<String> = _selectedFileName
    
    private val _selectedFileType = MutableLiveData<FileType>()
    val selectedFileType: LiveData<FileType> = _selectedFileType
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var selectedFile: File? = null
    
    fun setSelectedFile(file: File) {
        selectedFile = file
        _selectedFileName.value = file.name
        
        // Determine file type from extension
        val fileType = when {
            file.name.endsWith(".pdf", ignoreCase = true) -> FileType.PDF
            file.name.endsWith(".docx", ignoreCase = true) -> FileType.DOCX
            file.name.endsWith(".jpg", ignoreCase = true) -> FileType.JPG
            file.name.endsWith(".png", ignoreCase = true) -> FileType.PNG
            else -> FileType.PDF
        }
        _selectedFileType.value = fileType
        _fileSelected.value = true
        
        Log.d(TAG, "Dosya seçildi: ${file.name}, Tip: $fileType")
    }
    
    fun getSelectedFile(): File? {
        return selectedFile
    }
    
    fun getSelectedFileName(): String? {
        return selectedFile?.name
    }
    
    fun getSelectedFileType(): FileType? {
        return _selectedFileType.value
    }
} 