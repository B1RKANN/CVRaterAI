package com.cvraterai.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentUploadCvBinding
import com.cvraterai.myapplication.ui.cv.UploadCvViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UploadCvFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class UploadCvFragment : Fragment() {
    private val TAG = "UploadCvFragment"
    
    private var _binding: FragmentUploadCvBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UploadCvViewModel by viewModels()
    
    // File picker activity result
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }
    
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use view binding to inflate the layout
        _binding = FragmentUploadCvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        // Initially disable the Next button
        binding.cardNext.isEnabled = false
        binding.cardNext.alpha = 0.5f
        
        // Hide progress initially
        binding.cardProgress.visibility = View.GONE
        
        // Set click listener for upload CV button
        binding.tvUploadTitle.setOnClickListener {
            openFilePicker()
        }
        
        // Next button click listener
        binding.cardNext.setOnClickListener {
            navigateToCvRequired()
        }
    }
    
    private fun observeViewModel() {
        // Dosya seçildiğinde Next butonunu aktifleştir
        viewModel.fileSelected.observe(viewLifecycleOwner) { fileSelected ->
            binding.cardNext.isEnabled = fileSelected
            binding.cardNext.alpha = if (fileSelected) 1.0f else 0.5f
        }
        
        // Hata durumlarını gözlemle
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(requireContext(), "Hata: $it", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Hata: $it")
                }
            }
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",  // PDF
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // DOCX
                "image/jpeg",  // JPG
                "image/png"   // PNG
            ))
        }
        
        filePickerLauncher.launch(intent)
    }
    
    private fun handleSelectedFile(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = getFileNameFromUri(uri) ?: "cv_file"
            
            // Create a temporary file
            val tempFile = File(requireContext().cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Update the upload button text to show selected file
            binding.tvUploadTitle.text = "Seçildi: ${tempFile.name}"
            
            // Set the file in the ViewModel
            viewModel.setSelectedFile(tempFile)
            
            Log.d(TAG, "Dosya seçildi: ${tempFile.absolutePath}, boyut: ${tempFile.length()} byte")
            Toast.makeText(requireContext(), "CV dosyası seçildi", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Dosya seçme hatası: ${e.message}", e)
            Toast.makeText(requireContext(), "Dosya seçme hatası: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                it.getString(nameIndex)
            } else {
                uri.lastPathSegment
            }
        }
    }
    
    private fun navigateToCvRequired() {
        // Get the selected file from ViewModel
        val selectedFile = viewModel.getSelectedFile() ?: return
        
        // Create bundle with file path
        val bundle = bundleOf("filePath" to selectedFile.absolutePath)
        
        Log.d(TAG, "CvRequiredFragment'e geçiliyor, dosya yolu: ${selectedFile.absolutePath}")
        
        // Navigate to CvRequiredFragment with the bundle
        findNavController().navigate(R.id.action_uploadCvFragment_to_cvRequiredFragment, bundle)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}