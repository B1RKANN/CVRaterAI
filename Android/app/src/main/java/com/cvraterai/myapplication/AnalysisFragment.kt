package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cvraterai.myapplication.adapter.SkillRatingAdapter
import com.cvraterai.myapplication.databinding.FragmentAnalysisBinding
import com.cvraterai.myapplication.model.SkillRating
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnalysisFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalysisFragment : Fragment(), GestureDetector.OnGestureListener {
    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var gestureDetector: GestureDetectorCompat
    
    private var evaluationResponse: String? = null
    private var evaluationResultJson: String? = null
    private var evaluationId: Long = -1L
    private val skillRatings = mutableListOf<SkillRating>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            evaluationResponse = it.getString("evaluationResponse")
            evaluationResultJson = it.getString("evaluationResultJson")
            evaluationId = it.getLong("evaluationId", -1L)
        }
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
        
        // RecyclerView'ı ayarla
        setupRecyclerView()
        
        // Test için direkt olarak evaluate API yanıtındaki değeri kontrol et
        try {
            if (evaluationResponse != null) {
                val rawResponse = JSONObject(evaluationResponse!!)
                if (rawResponse.has("evaluationScore")) {
                    val directScore = rawResponse.getInt("evaluationScore")
                    android.util.Log.d("AnalysisFragment", "⭐⭐⭐ DOĞRUDAN API YANITI - evaluationScore: $directScore")
                    
                    // Değerleri hemen UI'a ayarla
                    binding.tvScorePercent.text = "%$directScore"
                    updateProgressBar(directScore)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Doğrudan yanıt değeri alınamadı: ${e.message}")
        }
        
        // Verileri yükle
        loadSkillRatingsFromJson()
        
        // Uyumluluk çubuğunu güncelle
        updateCompatibilityStatus()
        
        // Gesture detector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        
        // Ana layout'a dokunma olaylarını dinle
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        
        // RecyclerView için özel dokunma olayı ekle
        binding.recyclerViewSkills.setOnTouchListener(object : View.OnTouchListener {
            // Yatay kaydırma algılandığında bu değişkeni true yapacağız
            var isHorizontalSwipe = false
            // İlk dokunuş koordinatı
            var startX = 0f
            var startY = 0f
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // İlk dokunuşta koordinatları kaydet
                        startX = event.x
                        startY = event.y
                        isHorizontalSwipe = false
                        // RecyclerView'ın normal dikey kaydırmasını engelleme
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Yatay hareket dikey hareketten daha fazla ise
                        val deltaX = Math.abs(event.x - startX)
                        val deltaY = Math.abs(event.y - startY)
                        
                        if (deltaX > deltaY && deltaX > 50) {
                            isHorizontalSwipe = true
                            // RecyclerView'ın normal dikey kaydırmasını engelle
                            return true
                        }
                        
                        // Yatay kaydırma yoksa RecyclerView'ın normal davranışını sürdür
                        return isHorizontalSwipe
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isHorizontalSwipe) {
                            // Yatay kaydırma miktarı
                            val deltaX = event.x - startX
                            
                            // Sola kaydırma (sonraki sayfa)
                            if (deltaX < -100) {
                                navigateToAiNote()
                                return true
                            }
                            // Sağa kaydırma (önceki sayfa)
                            else if (deltaX > 100) {
                                findNavController().navigateUp()
                                return true
                            }
                        }
                        return isHorizontalSwipe
                    }
                }
                return false
            }
        })
        
        // Geri butonu 
        view.findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // İleri butonu
        view.findViewById<ImageButton>(R.id.btnNext)?.setOnClickListener {
            navigateToAiNote()
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
    
    private fun setupRecyclerView() {
        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSkills.adapter = SkillRatingAdapter(skillRatings)
    }
    
    private fun loadSkillRatingsFromJson() {
        if (evaluationResultJson != null) {
            try {
                android.util.Log.d("AnalysisFragment", "evaluationResultJson: $evaluationResultJson")
                val resultJson = JSONObject(evaluationResultJson!!)
                
                // skillRatings null kontrolü
                if (resultJson.has("skillRatings") && !resultJson.isNull("skillRatings")) {
                    val skillsArray = resultJson.getJSONArray("skillRatings")
                    
                    skillRatings.clear()
                    
                    // Beceri derecelendirmelerini ekle
                    for (i in 0 until skillsArray.length()) {
                        val skillObj = skillsArray.getJSONObject(i)
                        val skillName = skillObj.getString("language")
                        val skillRating = skillObj.getInt("percentage")
                        
                        skillRatings.add(SkillRating(skillName, skillRating))
                    }
                    
                    // Adapter'ı güncelle
                    binding.recyclerViewSkills.adapter?.notifyDataSetChanged()
                } else {
                    android.util.Log.d("AnalysisFragment", "skillRatings null veya mevcut değil")
                    
                    // Skills kısmını userInformation içindeki skills string'inden almaya çalış
                    if (resultJson.has("userInformation") && !resultJson.isNull("userInformation")) {
                        val userInfo = resultJson.getJSONObject("userInformation")
                        if (userInfo.has("skills") && !userInfo.isNull("skills")) {
                            val skillsString = userInfo.getString("skills")
                            android.util.Log.d("AnalysisFragment", "userInformation.skills: $skillsString")
                            
                            // Virgülle ayrılmış becerileri ayrıştır
                            val skillsList = skillsString.split(",", ", ")
                            
                            skillRatings.clear()
                            
                            // Her beceri için varsayılan bir oran ata (örn. 50)
                            for (skill in skillsList) {
                                val trimmedSkill = skill.trim()
                                if (trimmedSkill.isNotEmpty()) {
                                    skillRatings.add(SkillRating(trimmedSkill, 50))
                                }
                            }
                            
                            // Adapter'ı güncelle
                            binding.recyclerViewSkills.adapter?.notifyDataSetChanged()
                        }
                    }
                }
                
                // Uyumluluk çubuğunu güncelle
                updateCompatibilityStatus()
                
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "JSON işleme hatası: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun updateCompatibilityStatus() {
        try {
            // 0 gelmemesi için UI bileşenini hemen ayarla (varsayılan)
            binding.tvScorePercent.text = "%75"
            updateProgressBar(75)
            
            // Debug için JSON içeriğini logla
            android.util.Log.d("AnalysisFragment", "Evaluation Response: $evaluationResponse")
            android.util.Log.d("AnalysisFragment", "Evaluation Result JSON: $evaluationResultJson")
            
            // Önce direkt olarak evaluationResponse'tan score'u çıkarmayı dene
            try {
                val evalResponseObj = JSONObject(evaluationResponse ?: "{}")
                if (evalResponseObj.has("evaluationScore")) {
                    val score = evalResponseObj.getInt("evaluationScore")
                    android.util.Log.d("AnalysisFragment", "⭐ Direkt JSON'dan evaluationScore: $score")
                    
                    if (score > 0) {
                        // Yüzdelik değeri TextView'e ayarla
                        binding.tvScorePercent.text = "%$score"
                        
                        // Progress bar'ın weight değerlerini güncelle
                        updateProgressBar(score)
                        return
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "Direkt JSON çözümleme hatası: ${e.message}")
            }
            
            // Önce evaluationResultJson içinden compatibilityStatus değerini almayı deneyelim
            if (evaluationResultJson != null) {
                try {
                    val resultJson = JSONObject(evaluationResultJson)
                    if (resultJson.has("compatibilityStatus")) {
                        val compatibilityScore = resultJson.getInt("compatibilityStatus")
                        android.util.Log.d("AnalysisFragment", "⭐ compatibilityStatus from resultJson: $compatibilityScore")
                        
                        // Değeri direkt olarak kullan
                        binding.tvScorePercent.text = "%$compatibilityScore"
                        updateProgressBar(compatibilityScore)
                        return
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AnalysisFragment", "ResultJson çözümlenirken hata: ${e.message}")
                }
            }
            
            // Gson kullanarak evaluationResponse nesnesini çözümlemeyi dene
            val gson = com.google.gson.Gson()
            
            // İlk olarak CvEvaluationResponse olarak çözümle
            try {
                val evalResponse = gson.fromJson(evaluationResponse, com.cvraterai.myapplication.data.model.CvEvaluationResponse::class.java)
                if (evalResponse != null) {
                    var score = evalResponse.evaluationScore
                    android.util.Log.d("AnalysisFragment", "⭐ CvEvaluationResponse'dan orijinal evaluationScore: $score")
                    
                    // 1073741824 değerini kontrol et (hatalı değer olabilir)
                    if (score == 1073741824) {
                        // EvaluationResult içinden gerçek değeri almayı dene
                        try {
                            val resultObj = JSONObject(evalResponse.evaluationResult)
                            if (resultObj.has("compatibilityStatus")) {
                                score = resultObj.getInt("compatibilityStatus")
                                android.util.Log.d("AnalysisFragment", "⭐ compatibilityStatus'tan düzeltilen score: $score")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AnalysisFragment", "evaluationResult çözümlenirken hata: ${e.message}")
                            score = 75 // varsayılan değer
                        }
                    }
                    
                    android.util.Log.d("AnalysisFragment", "⭐ CvEvaluationResponse'dan Score değeri: $score")
                    
                    // Yüzdelik değeri TextView'e ayarla
                    if (score > 0) {
                        binding.tvScorePercent.text = "%$score"
                        
                        // Progress bar'ın weight değerlerini güncelle
                        updateProgressBar(score)
                    }
                    return
                }
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "CvEvaluationResponse çözümlenirken hata: ${e.message}")
            }
            
            // JSON olarak çözümlemeyi dene
            try {
                val responseJson = JSONObject(evaluationResponse ?: return)
                
                // evaluationScore değerini al
                var score = if (responseJson.has("evaluationScore")) {
                    responseJson.getInt("evaluationScore")
                } else if (responseJson.has("evaluation_score")) {
                    responseJson.getInt("evaluation_score")
                } else if (responseJson.has("score")) {
                    responseJson.getInt("score")
                } else {
                    android.util.Log.e("AnalysisFragment", "⭐ evaluationScore anahtarı bulunamadı")
                    0 // Varsayılan değer
                }
                
                // 1073741824 değerini kontrol et
                if (score == 1073741824) {
                    // EvaluationResult içinden gerçek değeri almayı dene
                    try {
                        if (responseJson.has("evaluationResult")) {
                            val resultObj = JSONObject(responseJson.getString("evaluationResult"))
                            if (resultObj.has("compatibilityStatus")) {
                                score = resultObj.getInt("compatibilityStatus")
                                android.util.Log.d("AnalysisFragment", "⭐ JSON compatibilityStatus'tan düzeltilen score: $score")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AnalysisFragment", "JSON evaluationResult çözümlenirken hata: ${e.message}")
                        score = 75 // varsayılan değer
                    }
                }
                
                android.util.Log.d("AnalysisFragment", "⭐ JSONObject'ten Score değeri: $score")
                
                // Yüzdelik değeri TextView'e ayarla
                if (score > 0) {
                    binding.tvScorePercent.text = "%$score"
                    
                    // Progress bar'ın weight değerlerini güncelle
                    updateProgressBar(score)
                }
            } catch (e: Exception) {
                android.util.Log.e("AnalysisFragment", "JSON çözümlenirken hata: ${e.message}")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Genel hata: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    private fun updateProgressBar(score: Int) {
        try {
            android.util.Log.d("AnalysisFragment", "⭐ updateProgressBar çağrıldı, score: $score")
            // Score değerini 0-100 arasına sınırla
            val boundedScore = score.coerceIn(0, 100)
            android.util.Log.d("AnalysisFragment", "⭐ boundedScore: $boundedScore")
            
            // Progress bar'ın weight değerlerini güncelle
            val filledLayoutParams = binding.compatibilityStatusBar.layoutParams as LinearLayout.LayoutParams
            filledLayoutParams.weight = boundedScore.toFloat()
            binding.compatibilityStatusBar.layoutParams = filledLayoutParams
            android.util.Log.d("AnalysisFragment", "⭐ filledLayoutParams.weight: ${filledLayoutParams.weight}")
            
            // Boş alanın weight değerini güncelle
            val remainingWeight = 100 - boundedScore
            android.util.Log.d("AnalysisFragment", "⭐ remainingWeight: $remainingWeight")
            
            // Doğrudan ID ile erişim
            val emptyLayoutParams = binding.emptyStatusBar.layoutParams as LinearLayout.LayoutParams
            emptyLayoutParams.weight = remainingWeight.toFloat()
            binding.emptyStatusBar.layoutParams = emptyLayoutParams
            android.util.Log.d("AnalysisFragment", "⭐ emptyLayoutParams.weight: ${emptyLayoutParams.weight}")
            
            // Force refresh - container adı progressCompatibility
            binding.progressCompatibility.requestLayout()
        } catch (e: Exception) {
            android.util.Log.e("AnalysisFragment", "Progress bar güncellenirken hata: ${e.message}", e)
        }
    }

    // GestureDetector.OnGestureListener metodları
    override fun onDown(e: MotionEvent): Boolean = false
    
    override fun onShowPress(e: MotionEvent) {}
    
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    
    override fun onLongPress(e: MotionEvent) {}
    
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        // Sola kaydırma hareketi algılandığında
        if (e1 != null && e2.x < e1.x && Math.abs(e1.x - e2.x) > 100 && Math.abs(velocityX) > 100) {
            navigateToAiNote()
            return true
        }
        // Sağa kaydırma hareketi algılandığında
        else if (e1 != null && e2.x > e1.x && Math.abs(e1.x - e2.x) > 100 && Math.abs(velocityX) > 100) {
            // InformationFragment'a geri dön
            findNavController().navigateUp()
            return true
        }
        return false
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
         * @return A new instance of fragment AnalysisFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = AnalysisFragment()
    }
}