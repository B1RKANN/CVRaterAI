package com.cvraterai.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentCvRequiredBinding
import com.cvraterai.myapplication.ui.cv.CvRequiredViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CvRequiredFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CvRequiredFragment : Fragment() {
    private val TAG = "CvRequiredFragment"
    
    private var _binding: FragmentCvRequiredBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CvRequiredViewModel by viewModels()
    
    private var selectedFile: File? = null
    
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get selected file path from arguments passed from UploadCvFragment
        arguments?.let {
            val filePath = it.getString("filePath")
            if (filePath != null) {
                selectedFile = File(filePath)
                if (selectedFile?.exists() == true) {
                    viewModel.setSelectedFile(selectedFile!!)
                    Log.d(TAG, "Dosya alındı: ${selectedFile?.absolutePath}, boyut: ${selectedFile?.length()} byte")
                } else {
                    Log.e(TAG, "Seçilen dosya bulunamadı: $filePath")
                }
            } else {
                Log.e(TAG, "Dosya yolu parametre olarak gelmedi")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use view binding to inflate the layout
        _binding = FragmentCvRequiredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        // Show selected file name if available
        selectedFile?.let {
            binding.tvSelectedFile.text = "Seçili Dosya: ${it.name}"
        }
        
        // Set click listener for Analyze CV button
        binding.cardAnalyzeButton.setOnClickListener {
            analyzeCv()
        }
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.cardAnalyzeButton.isEnabled = !isLoading
            binding.cardAnalyzeButton.alpha = if (isLoading) 0.5f else 1.0f
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe evaluation response
        viewModel.evaluationResponse.observe(viewLifecycleOwner) { response ->
            // Değerlendirme sonucu alındı, InformationFragment'e geçiş yap
            Log.d(TAG, "Değerlendirme başarılı: $response")
            
            // Gson ile evaluationResult string'ini JSON'a çeviriyoruz
            val gson = Gson()
            val evaluationResultJson = response.evaluationResult
            
            // Bundle oluştur ve değerlendirme sonucunu ekle
            val bundle = bundleOf(
                "evaluationResponse" to gson.toJson(response),
                "evaluationResultJson" to evaluationResultJson
            )
            
            // InformationFragment'e geçiş yap ve veriyi aktar
            findNavController().navigate(R.id.action_cvRequiredFragment_to_informationFragment, bundle)
        }
        
        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Hata: $it", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Değerlendirme hatası: $it")
            }
        }
    }
    
    private fun analyzeCv() {
        if (selectedFile == null || !selectedFile!!.exists()) {
            Toast.makeText(requireContext(), "Dosya bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get values from input fields (both are optional)
        val githubUrl = binding.etGithubLink.text.toString().takeIf { it.isNotEmpty() }
        val jobRequirements = binding.etRequiredFeatures.text.toString().takeIf { it.isNotEmpty() }
        
        Log.d(TAG, "CV değerlendirme başlatılıyor... GitHub URL: $githubUrl, Gereksinimler: $jobRequirements")
        
        // Call the API
        viewModel.evaluateCv(githubUrl, jobRequirements)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CvRequiredFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CvRequiredFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}