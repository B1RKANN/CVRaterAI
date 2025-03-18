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
        
        // RecyclerView'ı ayarla
        setupRecyclerView()
        
        // Verileri yükle
        loadSkillRatingsFromJson()
        
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
                val resultJson = JSONObject(evaluationResultJson!!)
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
                
                // Uyumluluk çubuğunu güncelle
                updateCompatibilityStatus()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateCompatibilityStatus() {
        try {
            // evaluationResponse'dan JSON nesnesi oluştur
            val responseJson = JSONObject(evaluationResponse ?: return)
            
            // evaluationScore değerini al
            val score = responseJson.getInt("evaluationScore")
            
            // Yüzdelik değeri TextView'e ayarla
            binding.tvScorePercent.text = "%$score"
            
            // Progress bar'ın weight değerlerini güncelle
            val filledLayoutParams = binding.compatibilityStatusBar.layoutParams as LinearLayout.LayoutParams
            filledLayoutParams.weight = score.toFloat()
            binding.compatibilityStatusBar.layoutParams = filledLayoutParams
            
            // Boş alanın weight değerini güncelle
            val remainingWeight = 100 - score
            val emptyView = (binding.compatibilityStatusBar.parent as LinearLayout).getChildAt(1)
            val emptyLayoutParams = emptyView.layoutParams as LinearLayout.LayoutParams
            emptyLayoutParams.weight = remainingWeight.toFloat()
            emptyView.layoutParams = emptyLayoutParams
            
        } catch (e: Exception) {
            e.printStackTrace()
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