package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.adapter.SkillRatingAdapter
import com.cvraterai.myapplication.databinding.FragmentAnalysisBinding
import com.cvraterai.myapplication.model.SkillRating
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.ProgressBar
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.animation.ArgbEvaluator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnalysisFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalysisFragment : Fragment() {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    
    private var evaluationResponse: String? = null
    private var evaluationResultJson: String? = null
    private var evaluationId: Long = -1L
    private val skillRatings = mutableListOf<SkillRating>()
    
    private lateinit var skillRatingAdapter: SkillRatingAdapter
    
    // Animasyonlar için static kontrol
    companion object {
        var animationsPlayed = false
        
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance() = AnalysisFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            evaluationResponse = it.getString("evaluationResponse")
            evaluationResultJson = it.getString("evaluationResultJson")
            evaluationId = it.getLong("evaluationId", -1L)
        }
        
        // Yeni bir fragment oluştuğunda, animasyon durumunu kontrol et ve log'la
        android.util.Log.d("AnalysisFragment", "onCreate: animationsPlayed = $animationsPlayed")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Debug için veri kaynağını logla
        android.util.Log.d("AnalysisFragment", "** onViewCreated çağrıldı **")
        android.util.Log.d("AnalysisFragment", "evaluationResponse var mı: ${evaluationResponse != null}")
        android.util.Log.d("AnalysisFragment", "evaluationResultJson var mı: ${evaluationResultJson != null}")
        android.util.Log.d("AnalysisFragment", "evaluationId: $evaluationId")
        android.util.Log.d("AnalysisFragment", "Animasyonlar daha önce oynatıldı mı: $animationsPlayed")
        
        // NestedScrollView'ın dikey kaydırma davranışını ayarlayalım
        configureNestedScrollView()
        
        // Force animationsPlayed to false to ensure animations play at least once in this session
        // This ensures animations will play when new fragment is opened
        if (!animationsPlayed) {
            android.util.Log.d("AnalysisFragment", "Animasyonlar ilk kez oynatılacak")
        }
        
        // SkillRatingAdapter'ı başlat
        skillRatingAdapter = SkillRatingAdapter(skillRatings)
        binding.recyclerViewSkills.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = skillRatingAdapter
        }
        
        // Verileri yükle
        loadSkillRatingsFromJson()
        
        // Uyumluluk çubuğunu API'den gelen verilere göre güncelle
        updateCompatibilityStatus()
        
        // Dokunma olayı dinleyicilerini ayarla
        setupNavigationControls()
        
        // Geri ve ileri butonlarını ayarla
        setupNavigationButtons()
    }
    
    private fun configureNestedScrollView() {
        try {
            // NestedScrollView'ı bulalım
            val nestedScrollView = binding.root as NestedScrollView
            
            // NestedScrollView için özel dokunma dinleyicisi
            nestedScrollView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Başlangıç noktası
                        v.tag = Pair(event.x, event.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val (startX, startY) = v.tag as? Pair<Float, Float> ?: Pair(0f, 0f)
                        val diffX = event.x - startX
                        val diffY = event.y - startY
                        
                        // Eğer yatay kaydırma dikey kaydırmadan belirgin şekilde fazlaysa
                        // (Bu durumda yatay kaydırmayı algılamak istiyoruz)
                        if (Math.abs(diffX) > Math.abs(diffY) * 1.8 && Math.abs(diffX) > 50) {
                            // Yatay kaydırma algılama olayını ebeveyn View'a geçirelim
                            v.parent.requestDisallowInterceptTouchEvent(false)
                        } 
                        // Eğer dikey kaydırma yatay kaydırmadan fazlaysa
                        else if (Math.abs(diffY) > Math.abs(diffX) * 1.2 && Math.abs(diffY) > 30) {
                            // NestedScrollView dikey kaydırmayı kendi yönetsin
                            v.parent.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Dokunma bittiğinde
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                // Olayı tüketmeyelim ki dokunma diğer dinleyicilere de gitsin
                false
            }
            
            // NestedScrollView kaydırma değişikliklerini dinle
            nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                // Eğer dikey kaydırma oluyorsa, yatay kaydırma algılanmasın
                if (Math.abs(scrollY - oldScrollY) > 10) {
                    nestedScrollView.requestDisallowInterceptTouchEvent(true)
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "NestedScrollView yapılandırma hatası: ${e.message}")
        }
    }
    
    private fun setupNavigationButtons() {
        try {
            // Geri butonu 
            binding.root.findViewById<ImageButton>(R.id.btnBack)?.apply { 
                setOnClickListener {
                    findNavController().navigateUp()
                }
                // Dokunma olayı davranışını düzeltmek için 
                isSoundEffectsEnabled = true
            }
            
            // İleri butonu
            binding.root.findViewById<ImageButton>(R.id.btnNext)?.apply {
                setOnClickListener {
                    navigateToAiNote()
                }
                // Dokunma olayı davranışını düzeltmek için
                isSoundEffectsEnabled = true
            }
            
            // Sayfa göstergelerini tıklanabilir yap
            binding.pageIndicators.setOnClickListener {
                navigateToAiNote()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Navigation buttons setup error: ${e.message}")
        }
    }
    
    private fun navigateToAiNote() {
        // AINoteFragment'a geçiş yap ve veriyi aktar
        val bundle = bundleOf(
            "evaluationResponse" to evaluationResponse,
            "evaluationResultJson" to evaluationResultJson,
            "evaluationId" to evaluationId
        )
        
        findNavController().navigate(R.id.action_analysisFragment_to_aiNoteFragment, bundle)
    }
    
    private fun loadSkillRatingsFromJson() {
        try {
            // API'dan gelen veriler boş değilse devam et
            if (evaluationResponse == null && evaluationResultJson == null) {
                android.util.Log.e("AnalysisFragment", "Yüklenecek veri yok - API yanıtı ve JSON null")
                return
            }

            // Önce RecyclerView'ı temizle
            skillRatings.clear()
            
            // JSON verisini skill ratings listesine dönüştür
            if (evaluationResultJson != null) {
                try {
                    val resultJson = JSONObject(evaluationResultJson!!)
                    
                    // skillRatings null kontrolü
                    if (resultJson.has("skillRatings") && !resultJson.isNull("skillRatings")) {
                        val skillsArray = resultJson.getJSONArray("skillRatings")
                        
                        // Beceri derecelendirmelerini ekle
                        for (i in 0 until skillsArray.length()) {
                            val skillObj = skillsArray.getJSONObject(i)
                            val skillName = skillObj.getString("language")
                            val skillRating = skillObj.getInt("percentage")
                            
                            skillRatings.add(SkillRating(skillName, skillRating))
                        }
                    } else {
                        android.util.Log.d("AnalysisFragment", "skillRatings null veya mevcut değil")
                        
                        // Skills kısmını userInformation içindeki skills string'inden almaya çalış
                        if (resultJson.has("userInformation") && !resultJson.isNull("userInformation")) {
                            val userInfo = resultJson.getJSONObject("userInformation")
                            if (userInfo.has("skills") && !userInfo.isNull("skills")) {
                                val skillsString = userInfo.getString("skills")
                                
                                // Virgülle ayrılmış becerileri ayrıştır
                                val skillsList = skillsString.split(",", ", ")
                                
                                // Her beceri için varsayılan bir oran ata (örn. 50)
                                for (skill in skillsList) {
                                    val trimmedSkill = skill.trim()
                                    if (trimmedSkill.isNotEmpty()) {
                                        skillRatings.add(SkillRating(trimmedSkill, 50))
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AnalysisFragment", "JSON işleme hatası: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Başarıyla verileri yükleyebildiğimizi logla
            android.util.Log.d("AnalysisFragment", "💥 Skill ratings yüklendi, eleman sayısı: ${skillRatings.size}")
            
            // Adapter'ı güncelle
            skillRatingAdapter.updateSkills(skillRatings)

            // İlk kez açıldığında animasyonları oynat, daha önce oynatıldıysa devre dışı bırak
            if (!animationsPlayed) {
                android.util.Log.d("AnalysisFragment", "💥 Animasyonlar ilk kez oynatılıyor")
                // Adapter'da default olarak animasyonlar zaten aktif
                
                // animationsPlayed değerini en sonda, animasyon başlarken değiştirme
                // Böylece hem SkillRating hem de Compatibility barlarının animasyonu tamamlanır
            } else {
                android.util.Log.d("AnalysisFragment", "💥 Animasyonlar daha önce oynatıldı")
                
                // Adapter'a animasyonları devre dışı bırak komutu ver
                skillRatingAdapter.disableAnimations()
            }

            // Uyumluluk yüzdesini güncelle
            var compatibilityScore = 75 // Varsayılan değer
            try {
                if (evaluationResponse != null) {
                    val evalResponseObj = JSONObject(evaluationResponse!!)
                    if (evalResponseObj.has("evaluationScore")) {
                        compatibilityScore = evalResponseObj.getInt("evaluationScore")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "Score değeri alınamadı: ${e.message}")
            }
            updateCompatibilityProgress(compatibilityScore)

        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Veri yüklenirken hata: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun updateCompatibilityStatus() {
        try {
            // Debug için JSON içeriğini logla
            android.util.Log.d("AnalysisFragment", "Evaluation Response: $evaluationResponse")
            android.util.Log.d("AnalysisFragment", "Evaluation Result JSON: $evaluationResultJson")
            
            // Son skor değerini bulmak için en güncel değeri kullanalım
            var finalScore = 75 // Varsayılan değer
            
            // Veri kaynaklarından skor değerini almaya çalış
            try {
                if (evaluationResultJson != null) {
                    val resultJson = JSONObject(evaluationResultJson)
                    if (resultJson.has("compatibilityStatus")) {
                        finalScore = resultJson.getInt("compatibilityStatus")
                    }
                } else if (evaluationResponse != null) {
                    val evalResponseObj = JSONObject(evaluationResponse)
                    if (evalResponseObj.has("evaluationScore")) {
                        finalScore = evalResponseObj.getInt("evaluationScore")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "Son skor değerini alma hatası: ${e.message}")
            }
            
            // Uyumluluk kartı için animasyonu ayarla
            updateCompatibilityCard(finalScore)
            
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Genel hata: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    // CardCompatibility için özel animasyon
    private fun updateCompatibilityCard(score: Int) {
        try {
            // Score değerini 0-100 arasına sınırla
            val boundedScore = score.coerceIn(0, 100)
            android.util.Log.d("AnalysisFragment", "💥 updateCompatibilityCard yeni: score=$boundedScore")
            
            if (!animationsPlayed) {
                android.util.Log.d("AnalysisFragment", "💥 Animasyonlar ilk kez oynatılacak")
                
                // Önce ilk durumu ayarla
                binding.cardCompatibility.alpha = 0f
                binding.cardCompatibility.translationY = 50f
                
                // ProgressBar'ı başlangıç durumuna getir - genişlik 0
                binding.progressCompatibility.post {
                    // Genişliği reset et
                    val progressBar = binding.compatibilityStatusBar
                    val params = progressBar.layoutParams
                    params.width = 0
                    progressBar.layoutParams = params
                    progressBar.requestLayout()
                    
                    // Yüzde göstergesini gizle
                    binding.tvScorePercent.text = "%0"
                    
                    if (_binding != null && binding.tvProgressPercent != null) {
                        binding.tvProgressPercent.text = "%0"
                        binding.tvProgressPercent.alpha = 0f
                    }
                    
                    // Kart animasyonunu başlat - sabit süre
                    binding.cardCompatibility.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400) // 0.4 saniye
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            // Kart animasyonu tamamlandığında progress bar animasyonunu başlat
                            android.util.Log.d("AnalysisFragment", "💥 Kart animasyonu tamamlandı, bar animasyonu başlıyor")
                            updateProgressBarWithAnimation(boundedScore)
                        }
                        .start()
                }
            } else {
                // Animasyon daha önce oynatıldıysa, kart zaten görünür olmalı
                binding.cardCompatibility.alpha = 1f
                binding.cardCompatibility.translationY = 0f
                
                // Bar'ı direkt olarak güncelle
                updateProgressBarWithoutAnimation(boundedScore)
            }
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "💥 updateCompatibilityCard hatası: ${e.message}")
        }
    }
    
    // Animasyonlu progress bar güncelleme - ilk kez gösterildiğinde kullanılır
    private fun updateProgressBarWithAnimation(score: Int) {
        try {
            android.util.Log.d("AnalysisFragment", "💥 updateProgressBarWithAnimation yeni: score=$score")
            val boundedScore = score.coerceIn(0, 100)
            
            // Container genişliğini al
            val containerWidth = binding.progressCompatibility.width
            if (containerWidth <= 0) {
                android.util.Log.e("AnalysisFragment", "💥 Container genişliği 0 veya negatif")
                return
            }
            
            // İlk durumu ayarla - 0 göster
            binding.tvScorePercent.text = "%0"
            
            // Progress barı sıfırla
            val progressBar = binding.compatibilityStatusBar
            val params = progressBar.layoutParams
            params.width = 0
            progressBar.layoutParams = params
            progressBar.requestLayout()
            
            // Göstergeyi hazırla ve gizle
            if (_binding != null && binding.tvProgressPercent != null) {
                binding.tvProgressPercent.text = "%0"
                binding.tvProgressPercent.alpha = 0f
            }
            
            // Gecikme ile başlat (daha görünür animasyon için)
            progressBar.postDelayed({
                // SkillRatingAdapter'daki gibi ValueAnimator kullan
                val animator = ValueAnimator.ofInt(0, boundedScore)
                animator.duration = 1000 // 1 saniye
                animator.interpolator = DecelerateInterpolator()
                
                animator.addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Int
                    
                    try {
                        // Ana yüzde metnini güncelle
                        binding.tvScorePercent.text = "%$animatedValue"
                        
                        // Bar genişliğini animasyonlu olarak arttır
                        val newWidth = (containerWidth * animatedValue) / 100
                        val layoutParams = progressBar.layoutParams
                        layoutParams.width = newWidth
                        progressBar.layoutParams = layoutParams
                        
                        // Yüzde göstergesini güncelle
                        if (_binding != null && binding.tvProgressPercent != null) {
                            // Göstergeyi belirli bir eşikten sonra göster
                            if (animatedValue >= 20) {
                                if (binding.tvProgressPercent.alpha == 0f) {
                                    // İlk kez görünür olacak
                                    binding.tvProgressPercent.alpha = 1f
                                }
                                
                                // Text'i güncelle
                                binding.tvProgressPercent.text = "%$animatedValue"
                                
                                // Göstergeyi doğru konumda göster
                                if (newWidth > 0) {
                                    // Göstergeyi barın ucuna konumlandır
                                    val xPosition = newWidth - binding.tvProgressPercent.width
                                    binding.tvProgressPercent.translationX = Math.max(xPosition, 0).toFloat()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AnalysisFragment", "💥 Animasyon update hatası: ${e.message}")
                    }
                }
                
                // Animasyon bittiğinde animationsPlayed'i true yap
                animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        animationsPlayed = true
                        android.util.Log.d("AnalysisFragment", "💥 Animasyon tamamlandı - animationsPlayed = true olarak işaretlendi")
                    }
                })
                
                // Animasyonu başlat
                animator.start()
                android.util.Log.d("AnalysisFragment", "💥 Bar animasyonu başlatıldı - width=$containerWidth")
            }, 200) // 200ms gecikme ile başlat (kart animasyonundan sonra)
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "💥 updateProgressBarWithAnimation hatası: ${e.message}")
            updateProgressBarWithoutAnimation(score)
        }
    }
    
    // Animasyonsuz progress bar güncelleme - zaten animasyon oynatılmışsa kullanılır
    private fun updateProgressBarWithoutAnimation(score: Int) {
        try {
            android.util.Log.d("AnalysisFragment", "Animasyonsuz bar güncellemesi: score=$score")
            val boundedScore = score.coerceIn(0, 100)
            binding.tvScorePercent.text = "%$boundedScore"
            
            // Progress bar'ı güncelle
            binding.progressCompatibility.post {
                val progressBar = binding.compatibilityStatusBar
                val containerWidth = binding.progressCompatibility.width
                if (containerWidth > 0) {
                    val layoutParams = progressBar.layoutParams
                    layoutParams.width = (containerWidth * boundedScore) / 100
                    progressBar.layoutParams = layoutParams
                    progressBar.requestLayout()
                    
                    // Göstergeyi doğru konumlandır ve görünür yap
                    if (_binding != null && binding.tvProgressPercent != null) {
                        binding.tvProgressPercent.text = "%$boundedScore"
                        binding.tvProgressPercent.alpha = 1f
                        
                        // Bar'ın genişliğine göre göstergeyi konumlandır
                        val finalWidth = (containerWidth * boundedScore) / 100
                        val position = Math.max(finalWidth - binding.tvProgressPercent.width, 0)
                        binding.tvProgressPercent.translationX = position.toFloat()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Animasyonsuz bar hatası: ${e.message}")
        }
    }
    
    private fun updateProgressBar(score: Int) {
        if (!animationsPlayed) {
            updateProgressBarWithAnimation(score)
        } else {
            updateProgressBarWithoutAnimation(score)
        }
    }

    // Daha güvenli bir şekilde kaydırma algılamasını yönet
    private fun setupNavigationControls() {
        try {
            val gestureDetector = android.view.GestureDetector(requireContext(), object : android.view.GestureDetector.SimpleOnGestureListener() {
                // Dikey kaydırmaya göre yatay kaydırmanın daha kolay algılanması için değerler
                private val SWIPE_THRESHOLD = 80
                private val SWIPE_VELOCITY_THRESHOLD = 80

                override fun onDown(e: MotionEvent): Boolean {
                    // onDown'da true döndürerek tüm hareketlerin algılanmasını sağla
                    return true
                }

                override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                    val diffX = e2.x - (e1?.x ?: 0f)
                    val diffY = e2.y - (e1?.y ?: 0f)

                    try {
                        // Yatay kaydırma önemli ölçüde dikey kaydırmadan fazlaysa
                        if (Math.abs(diffX) > Math.abs(diffY) * 1.5 && 
                            Math.abs(diffX) > SWIPE_THRESHOLD && 
                            Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            
                            if (diffX > 0) {
                                // Sağa kaydırma - önceki sayfaya git
                                android.util.Log.d("AnalysisFragment", "Sağa kaydırma algılandı - Önceki sayfa")
                                findNavController().navigateUp()
                                return true
                            } else {
                                // Sola kaydırma - sonraki sayfaya git
                                android.util.Log.d("AnalysisFragment", "Sola kaydırma algılandı - Sonraki sayfa")
                                navigateToAiNote()
                                return true
                            }
                        }
                    } catch (exception: Exception) {
                        android.util.Log.e("AnalysisFragment", "Kaydırma hatası: ${exception.message}")
                    }
                    return false
                }
            })

            // Root view'a touch listener ekle
            val rootView = binding.root
            rootView.setOnTouchListener { v, event ->
                // Gesture detector'a olayları ilet
                val consumed = gestureDetector.onTouchEvent(event)
                
                // Eğer dokunma olayı tüketilmediyse, normal davranışı devam ettir
                if (!consumed) {
                    v.performClick()
                }
                false
            }
            
            // CardCompatibility özel kaydırma algılama kaldırıldı
            
            // Ayrıca doğrudan CardCompatibility için hassas kaydırma algılama kaldırıldı
            
            // Kart tıklaması kaldırıldı
            
            // Tüm alt view'lara da touch listenerleri ekle, ama özel durumları ele al
            addSwipeDetectionToSpecificChildren()
            
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "setupNavigationControls hatası: ${e.message}")
        }
    }
    
    // CardCompatibility'ye hassas kaydırma algılama ekle - iptal edildi
    private fun addSwipeToCard() {
        // Bu fonksiyon artık kullanılmıyor
    }
    
    // RecyclerView ve CardCompatibility hariç diğer view'lara kaydırma algılama ekle
    private fun addSwipeDetectionToSpecificChildren() {
        try {
            // ConstraintLayout gibi önemli gruplar için kaydırma algıla
            val layoutGroups = listOf(
                binding.root.findViewById<ViewGroup>(R.id.navigationButtons),
                binding.pageIndicators
            )
            
            // Her bir gruba kaydırma algılamayı ekle
            layoutGroups.forEach { viewGroup ->
                viewGroup?.setOnTouchListener { _, event ->
                    // Olayı ana activite'ye ilet, ama tüketme
                    activity?.dispatchTouchEvent(event)
                    false
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "addSwipeDetectionToSpecificChildren hatası: ${e.message}")
        }
    }

    private fun updateSkillRatings(skills: List<SkillRating>) {
        skillRatingAdapter.updateSkills(skills)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateCompatibilityProgress(percentage: Int) {
        try {
            // Renk sabitleri
            val colorRed = Color.parseColor("#FF4B4B")
            val colorOrange = Color.parseColor("#FFA726")
            val colorGreen = Color.parseColor("#4CAF50")

            // Progress bar genişliğini ayarla
            val statusBar = binding.compatibilityStatusBar
            val container = statusBar.parent as View
            val containerWidth = container.width

            // Animasyon için ValueAnimator oluştur
            val animator = ValueAnimator.ofFloat(0f, percentage.toFloat())
            animator.duration = 1500 // 1.5 saniye
            animator.interpolator = DecelerateInterpolator()

            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val currentPercentage = animatedValue.toInt()

                // Genişliği güncelle
                val params = statusBar.layoutParams
                params.width = (containerWidth * (currentPercentage / 100f)).toInt()
                statusBar.layoutParams = params

                // Yüzde metnini güncelle
                binding.tvProgressPercent.text = "%$currentPercentage"
                binding.tvScorePercent.text = "%$currentPercentage"

                // Renk geçişi için GradientDrawable oluştur
                val drawable = GradientDrawable()
                drawable.cornerRadius = resources.getDimension(R.dimen.progress_corner_radius)

                // Yüzdeye göre renk geçişi
                val currentColor = when {
                    currentPercentage < 25 -> colorRed
                    currentPercentage < 50 -> interpolateColor(colorRed, colorOrange, (currentPercentage - 25) / 25f)
                    currentPercentage < 70 -> interpolateColor(colorOrange, colorGreen, (currentPercentage - 50) / 20f)
                    else -> colorGreen
                }

                drawable.setColor(currentColor)
                statusBar.background = drawable

                // Yüzde göstergesinin rengini güncelle
                binding.tvProgressPercent.setTextColor(currentColor)
                binding.tvProgressPercent.alpha = 1f

                // Yüzde göstergesinin konumunu güncelle
                binding.tvProgressPercent.post {
                    try {
                        val progressWidth = (containerWidth * (currentPercentage / 100f)).toInt()
                        val indicatorWidth = binding.tvProgressPercent.width
                        
                        // Göstergenin konumunu hesapla
                        val newX = when {
                            // Progress bar çok küçükse, göstergeyi en başta tut
                            progressWidth <= indicatorWidth -> 0f
                            
                            // Progress bar yeterince genişse, göstergeyi progress bar'ın sonuna yerleştir
                            else -> {
                                val position = progressWidth - indicatorWidth
                                position.coerceIn(0, containerWidth - indicatorWidth).toFloat()
                            }
                        }
                        
                        binding.tvProgressPercent.translationX = newX
                    } catch (e: Exception) {
                        android.util.Log.e("AnalysisFragment", "Gösterge konumu hesaplanırken hata: ${e.message}")
                    }
                }

                // Score yüzdesinin rengini de güncelle
                binding.tvScorePercent.setTextColor(currentColor)
            }

            // Animasyonu başlat
            animator.start()

        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Progress güncellenirken hata: ${e.message}")
        }
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        return ArgbEvaluator().evaluate(fraction, startColor, endColor) as Int
    }
}