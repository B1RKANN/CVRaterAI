package com.cvraterai.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.cvraterai.myapplication.databinding.FragmentCvRequiredBinding
import com.cvraterai.myapplication.ui.cv.CvRequiredViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import android.widget.TextView

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
            val selectedFileText = if (isEnglishLanguage()) {
                getString(R.string.selected_file, it.name)
            } else {
                getString(R.string.selected_file_tr, it.name)
            }
            binding.tvSelectedFile.text = selectedFileText
        }
        
        // Set click listener for Analyze CV button
        binding.cardAnalyzeButton.setOnClickListener {
            analyzeCv()
        }
        
        // Set click listener for Cancel button in loading overlay
        binding.btnCancelAnalysis.setOnClickListener {
            cancelAnalysis()
        }
        
        // Set cancel button text based on language (use TextView inside CardView)
        val cancelText = if (isEnglishLanguage()) {
            getString(R.string.cancel_analysis)
        } else {
            getString(R.string.cancel_analysis_tr)
        }
        
        // Adjust the TextView inside the CardView
        (binding.btnCancelAnalysis.findViewById<TextView>(R.id.tvCancelText))?.let {
            it.text = cancelText
        }
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.cardAnalyzeButton.isEnabled = !isLoading
            binding.cardAnalyzeButton.alpha = if (isLoading) 0.5f else 1.0f
            binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            
            if (isLoading) {
                startLoadingAnimation()
            }
        }
        
        // Observe process step
        viewModel.processStep.observe(viewLifecycleOwner) { step ->
            updateLoadingStep(step)
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
        
        // Observe errors - but only show errors that are not caused by user cancellation
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null && viewModel.loading.value == false) {
                // Dil ayarına göre hata mesajı göster
                val errorPrefix = if (isEnglishLanguage()) "Error: " else "Hata: "
                Toast.makeText(requireContext(), errorPrefix + error, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Değerlendirme hatası: $error")
            }
        }
    }
    
    /**
     * Yükleme animasyonunu başlat
     */
    private fun startLoadingAnimation() {
        // Yükleme mesajlarını güncelle
        if (isEnglishLanguage()) {
            binding.tvLoadingText.text = getString(R.string.loading_title_en)
            binding.tvLoadingSubText.text = getString(R.string.loading_subtitle_en)
            binding.tvProcessStep.text = getString(R.string.process_step_initializing)
            
            // Yüzde göstergesi altındaki "Tamamlandı" metnini güncelle
            binding.tvCompleted.text = getString(R.string.completed)
            
            // İlerleme yazısını güncelle
            binding.tvProgress.text = getString(R.string.progress)
        } else {
            binding.tvLoadingText.text = getString(R.string.loading_title)
            binding.tvLoadingSubText.text = getString(R.string.loading_subtitle)
            binding.tvProcessStep.text = getString(R.string.process_step_initializing_tr)
            
            // Yüzde göstergesi altındaki "Tamamlandı" metnini güncelle
            binding.tvCompleted.text = getString(R.string.completed_tr)
            
            // İlerleme yazısını güncelle
            binding.tvProgress.text = getString(R.string.progress_tr)
        }
        
        // Animasyon ayarlarını güncelle
        binding.loadingAnimation.repeatCount = LottieDrawable.INFINITE
        binding.loadingAnimation.playAnimation()
        
        // İlerleme yüzdesini simule etmek için sayaç başlat
        simulateProgress()
    }
    
    /**
     * İlerleme yüzdesini simule et
     */
    private var currentProgress = 0
    private var progressHandler: Handler? = null
    
    private fun simulateProgress() {
        currentProgress = 0
        binding.tvProgressPercentInner.text = "0%"
        
        progressHandler = Handler(Looper.getMainLooper())
        
        val progressRunnable = object : Runnable {
            override fun run() {
                // İlerleme yüzdesini artır (0-95 arası)
                if (currentProgress < 95) {
                    currentProgress += 5
                    binding.tvProgressPercentInner.text = "$currentProgress%"
                    // Her adımda farklı bir gecikme ile çağır (daha gerçekçi görünmesi için)
                    progressHandler?.postDelayed(this, (500..1500).random().toLong())
                }
            }
        }
        
        // İlk çağrıyı başlat
        progressHandler?.post(progressRunnable)
    }
    
    /**
     * Yükleme adımını güncelle
     */
    private fun updateLoadingStep(step: CvRequiredViewModel.ProcessStep) {
        when (step) {
            CvRequiredViewModel.ProcessStep.UPLOADING -> {
                binding.tvProcessStep.text = if (isEnglishLanguage()) {
                    getString(R.string.process_step_uploading)
                } else {
                    getString(R.string.process_step_uploading_tr)
                }
                currentProgress = 10
            }
            CvRequiredViewModel.ProcessStep.PARSING -> {
                binding.tvProcessStep.text = if (isEnglishLanguage()) {
                    getString(R.string.process_step_parsing)
                } else {
                    getString(R.string.process_step_parsing_tr)
                }
                currentProgress = 30
            }
            CvRequiredViewModel.ProcessStep.ANALYZING -> {
                binding.tvProcessStep.text = if (isEnglishLanguage()) {
                    getString(R.string.process_step_analyzing)
                } else {
                    getString(R.string.process_step_analyzing_tr)
                }
                currentProgress = 50
            }
            CvRequiredViewModel.ProcessStep.SCORING -> {
                binding.tvProcessStep.text = if (isEnglishLanguage()) {
                    getString(R.string.process_step_scoring)
                } else {
                    getString(R.string.process_step_scoring_tr)
                }
                currentProgress = 70
            }
            CvRequiredViewModel.ProcessStep.FINALIZING -> {
                binding.tvProcessStep.text = if (isEnglishLanguage()) {
                    getString(R.string.process_step_finalizing)
                } else {
                    getString(R.string.process_step_finalizing_tr)
                }
                currentProgress = 90
            }
        }
        binding.tvProgressPercentInner.text = "$currentProgress%"
    }
    
    private fun analyzeCv() {
        if (selectedFile == null || !selectedFile!!.exists()) {
            val fileNotFoundMsg = if (isEnglishLanguage()) {
                getString(R.string.file_not_found)
            } else {
                getString(R.string.file_not_found_tr)
            }
            Toast.makeText(requireContext(), fileNotFoundMsg, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get values from input fields (both are optional)
        val githubUrl = binding.etGithubLink.text.toString().takeIf { it.isNotEmpty() }
        val jobRequirements = binding.etRequiredFeatures.text.toString().takeIf { it.isNotEmpty() }
        
        Log.d(TAG, "CV değerlendirme başlatılıyor... GitHub URL: $githubUrl, Gereksinimler: $jobRequirements")
        
        // Call the API
        viewModel.evaluateCv(githubUrl, jobRequirements)
    }
    
    private fun cancelAnalysis() {
        Log.d(TAG, "Kullanıcı CV değerlendirme işlemini iptal etti")
        // İlerleme simülasyonunu durdur
        progressHandler?.removeCallbacksAndMessages(null)
        // İşlemi iptal et
        viewModel.cancelEvaluation()
        
        // İşlem iptal edildi mesajını göster
        val cancelMessage = if (isEnglishLanguage()) {
            getString(R.string.process_canceled)
        } else {
            getString(R.string.process_canceled_tr)
        }
        Toast.makeText(requireContext(), cancelMessage, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Dil İngilizce mi kontrol et
     */
    private fun isEnglishLanguage(): Boolean {
        return resources.configuration.locales[0].language.startsWith("en", ignoreCase = true)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // İlerleme simülasyonunu temizle
        progressHandler?.removeCallbacksAndMessages(null)
        progressHandler = null
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