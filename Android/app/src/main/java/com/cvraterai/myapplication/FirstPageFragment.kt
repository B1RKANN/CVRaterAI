package com.cvraterai.myapplication

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.data.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import android.view.animation.AnimationUtils
import com.cvraterai.myapplication.databinding.FragmentFirstPageBinding
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FirstPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    
    @Inject
    lateinit var authRepository: AuthRepository

    private var _binding: FragmentFirstPageBinding? = null
    private val binding get() = _binding!!

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
    ): View? {
        _binding = FragmentFirstPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Start floating animation for robot with scale
        val robotAnimation = AnimationSet(true).apply {
            val float = AnimationUtils.loadAnimation(requireContext(), R.anim.floating_animation)
            val scale = ScaleAnimation(
                1f, 1.05f, 1f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 2000
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            }
            addAnimation(float)
            addAnimation(scale)
        }
        binding.ivChatbot.startAnimation(robotAnimation)
        
        // Add fade in and slide up animation for title and subtitle
        val titleAnim = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in).apply {
            duration = 1000
        }
        binding.titleText.startAnimation(titleAnim)
        
        val subtitleAnim = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in).apply {
            duration = 1000
            startOffset = 500
        }
        binding.subtitleText.startAnimation(subtitleAnim)

        // Start bubble animations
        startBubbleAnimations()
        
        // Setup language buttons and click listeners
        setupLanguageButtons(view)
        setupClickListeners()
    }

    private fun startBubbleAnimations() {
        val bubbles = listOf(
            binding.bubble1, binding.bubble2, binding.bubble3, binding.bubble4, binding.bubble5,
            binding.bubble6, binding.bubble7, binding.bubble8, binding.bubble9, binding.bubble10,
            binding.bubble11, binding.bubble12, binding.bubble13, binding.bubble14, binding.bubble15,
            binding.bubble16, binding.bubble17, binding.bubble18, binding.bubble19, binding.bubble20,
            binding.bubble21, binding.bubble22, binding.bubble23, binding.bubble24, binding.bubble25,
            binding.bubble26, binding.bubble27, binding.bubble28, binding.bubble29, binding.bubble30
        )

        // Tüm baloncukları animasyonlarla hareketlendirme
        bubbles.forEachIndexed { index, bubble ->
            // Her baloncuk için özel ve random değerler hesapla - mesafeleri arttırdım
            val baseDistance = 1.0f + (Math.random() * 1.2f).toFloat() // 1.0 ile 2.2 arası (önceki 0.5-1.2 yerine)
            val baseDuration = 4000 + (Math.random() * 3000).toLong()  // 4-7 saniye arası
            val startDelay = (index * 100).toLong()  // Baloncukların kademeli başlaması için gecikme
            
            // Animasyon stili: Çeşitli kombinasyonlar
            val animationType = index % 5
            
            when (animationType) {
                0 -> {
                    // Diagonal hareket - hem x hem y'de haraket - daha belirgin
                    val diagonalAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, baseDistance * (if (index % 2 == 0) 1 else -1),
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, -baseDistance * 1.2f
                    ).apply {
                        duration = baseDuration
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    // Aynı zamanda saydamlık ve daha büyük boyut değişimi
                    val alphaAnim = AlphaAnimation(1.0f, 0.4f).apply {
                        duration = baseDuration - 500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val scaleAnim = ScaleAnimation(
                        1f, 1.5f, 1f, 1.5f, // Daha büyük boyut değişimi
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = baseDuration + 500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val animSet = AnimationSet(false).apply {
                        addAnimation(diagonalAnim)
                        addAnimation(alphaAnim)
                        addAnimation(scaleAnim)
                    }
                    
                    bubble.startAnimation(animSet)
                }
                1 -> {
                    // Sadece yatay hareket - daha uzağa
                    val horizontalAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, baseDistance * 1.3f * (if (index % 2 == 0) 1 else -1),
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f
                    ).apply {
                        duration = baseDuration
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val alphaAnim = AlphaAnimation(1.0f, 0.3f).apply {
                        duration = baseDuration - 800
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    // Hafif boyut değişimi ekle
                    val scaleAnim = ScaleAnimation(
                        1f, 1.3f, 1f, 1.3f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = baseDuration - 300
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay + 200
                    }
                    
                    val animSet = AnimationSet(false).apply {
                        addAnimation(horizontalAnim)
                        addAnimation(alphaAnim)
                        addAnimation(scaleAnim)
                    }
                    
                    bubble.startAnimation(animSet)
                }
                2 -> {
                    // Sadece dikey hareket (daha uzun)
                    val verticalAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, -baseDistance * 2.0f // İki kat daha uzağa hareket
                    ).apply {
                        duration = baseDuration + 1000
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    // Daha dramatik boyut değişimi
                    val scaleAnim = ScaleAnimation(
                        1f, 0.7f, 1f, 0.7f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = baseDuration - 200
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val animSet = AnimationSet(false).apply {
                        addAnimation(verticalAnim)
                        addAnimation(scaleAnim)
                    }
                    
                    bubble.startAnimation(animSet)
                }
                3 -> {
                    // Daire benzeri hareket (iki ayrı animasyon kombinasyonu)
                    val horizontalAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, baseDistance * 1.5f * (if (index % 2 == 0) 1 else -1),
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f
                    ).apply {
                        duration = baseDuration - 1000
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val verticalAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, -baseDistance * 1.4f
                    ).apply {
                        duration = baseDuration
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay + 500 // Hafif gecikme ekleyerek asenkron hareket
                    }
                    
                    val alphaAnim = AlphaAnimation(1.0f, 0.2f).apply {
                        duration = baseDuration - 500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    // Önce yatay hareketi başlat
                    bubble.startAnimation(horizontalAnim)
                    
                    // 500ms sonra dikey ve alpha animasyonlarını başlat
                    val secondSet = AnimationSet(false).apply {
                        addAnimation(verticalAnim)
                        addAnimation(alphaAnim)
                    }
                    
                    bubble.postDelayed({
                        if (isAdded && bubble.isAttachedToWindow) {  // Fragment halen aktifse
                            bubble.startAnimation(secondSet)
                        }
                    }, 500)
                }
                else -> {
                    // Büyüyüp küçülen, dönen ve hareket eden animasyon
                    val pulseAnim = ScaleAnimation(
                        1f, 1.7f, 1f, 1.7f, // Daha büyük ölçek değişimi
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = baseDuration
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val moveAnim = TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, baseDistance * 0.9f * (if (index % 2 == 0) 1 else -1),
                        Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, -baseDistance * 1.1f
                    ).apply {
                        duration = baseDuration + 500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                        startOffset = startDelay
                    }
                    
                    val animSet = AnimationSet(false).apply {
                        addAnimation(pulseAnim)
                        addAnimation(moveAnim)
                    }
                    
                    bubble.startAnimation(animSet)
                }
            }
        }
    }
    
    // Mevcut dil ayarına göre butonları göster/gizle
    private fun setupLanguageButtons(view: View) {
        // Mevcut dil ayarını al
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentLanguage = sharedPreferences.getString("language", "en") ?: "en"
        
        // Dil Türkçe ise İngilizce butonunu göster, Türkçe butonunu gizle
        if (currentLanguage == "tr") {
            binding.btnTurkceDevamEt.visibility = View.GONE
            binding.btnContinueInEnglish.visibility = View.VISIBLE
        } else {
            // Dil İngilizce ise Türkçe butonunu göster, İngilizce butonunu gizle
            binding.btnTurkceDevamEt.visibility = View.VISIBLE
            binding.btnContinueInEnglish.visibility = View.GONE
        }
    }
    
    private fun setupClickListeners() {
        // Get Started butonuna tıklama olayını ayarla
        binding.btnGetStarted.setOnClickListener {
            // Kullanıcı giriş yapmışsa doğrudan HomePage'e yönlendir
            if (authRepository.isLoggedIn()) {
                findNavController().navigate(R.id.action_firstPageFragment_to_homePageFragment)
            } else {
                // Kullanıcı giriş yapmamışsa normal akışa devam et
                findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
            }
        }
        
        // Türkçe devam et butonuna tıklama olayını ayarla
        binding.btnTurkceDevamEt.setOnClickListener {
            // Dili Türkçe'ye çevir
            setLocale("tr")
            
            // Kullanıcıya bilgi ver
            Toast.makeText(requireContext(), "Dil Türkçe olarak değiştirildi", Toast.LENGTH_SHORT).show()
            
            // Kullanıcı giriş yapmışsa doğrudan HomePage'e yönlendir
            if (authRepository.isLoggedIn()) {
                findNavController().navigate(R.id.action_firstPageFragment_to_homePageFragment)
            } else {
                // Kullanıcı giriş yapmamışsa normal akışa devam et
                findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
            }
        }
        
        // İngilizce devam et butonuna tıklama olayını ayarla
        binding.btnContinueInEnglish.setOnClickListener {
            // Dili İngilizce'ye çevir
            setLocale("en")
            
            // Kullanıcıya bilgi ver
            Toast.makeText(requireContext(), "Language changed to English", Toast.LENGTH_SHORT).show()
            
            // Kullanıcı giriş yapmışsa doğrudan HomePage'e yönlendir
            if (authRepository.isLoggedIn()) {
                findNavController().navigate(R.id.action_firstPageFragment_to_homePageFragment)
            } else {
                // Kullanıcı giriş yapmamışsa normal akışa devam et
                findNavController().navigate(R.id.action_firstPageFragment_to_step1Fragment)
            }
        }
    }
    
    // Dil değiştirme fonksiyonu
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(requireContext().resources.configuration)
        config.setLocale(locale)
        
        // Uygulama genelinde dil ayarını kaydet
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", languageCode).apply()
        
        // Kaynakları güncelle
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().recreate()
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
         * @return A new instance of fragment FirstPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}