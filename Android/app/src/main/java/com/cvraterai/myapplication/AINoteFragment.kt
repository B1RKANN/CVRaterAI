package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentAiNoteBinding
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AINoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AINoteFragment : Fragment(), GestureDetector.OnGestureListener {
    private var _binding: FragmentAiNoteBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var gestureDetector: GestureDetectorCompat
    
    private var evaluationResponse: String? = null
    private var evaluationResultJson: String? = null
    private var evaluationId: Long = -1L
    private var fromHistory: Boolean = false

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
        _binding = FragmentAiNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Verileri göster
        displayAiNote()
        
        // Gesture detector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        
        // Ana layout'a dokunma olaylarını dinle
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        
        // ScrollView için özel dokunma olayı ekle
        binding.scrollView.setOnTouchListener(object : View.OnTouchListener {
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
                        // ScrollView'in normal dikey kaydırmasını engelleme
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Yatay hareket dikey hareketten daha fazla ise
                        val deltaX = Math.abs(event.x - startX)
                        val deltaY = Math.abs(event.y - startY)
                        
                        if (deltaX > deltaY && deltaX > 50) {
                            isHorizontalSwipe = true
                            // ScrollView'in normal dikey kaydırmasını engelle
                            return true
                        }
                        
                        // Yatay kaydırma yoksa ScrollView'in normal davranışını sürdür
                        return isHorizontalSwipe
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isHorizontalSwipe) {
                            // Yatay kaydırma miktarı
                            val deltaX = event.x - startX
                            
                            // Sağa kaydırma (önceki sayfa)
                            if (deltaX > 100) {
                                findNavController().navigateUp() // AnalysisFragment'a geri dön
                                return true
                            }
                        }
                        return isHorizontalSwipe
                    }
                }
                return false
            }
        })
        
        // tvNoteContent için özel dokunma olayı ekle
        binding.tvNoteContent.setOnTouchListener(object : View.OnTouchListener {
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
                        // TextView'in normal davranışını engelleme
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Yatay hareket dikey hareketten daha fazla ise
                        val deltaX = Math.abs(event.x - startX)
                        val deltaY = Math.abs(event.y - startY)
                        
                        if (deltaX > deltaY && deltaX > 50) {
                            isHorizontalSwipe = true
                            // TextView'in normal dikey kaydırmasını engelle
                            return true
                        }
                        
                        // Yatay kaydırma yoksa TextView'in normal davranışını sürdür
                        return isHorizontalSwipe
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isHorizontalSwipe) {
                            // Yatay kaydırma miktarı
                            val deltaX = event.x - startX
                            
                            // Sağa kaydırma (önceki sayfa)
                            if (deltaX > 100) {
                                findNavController().navigateUp() // AnalysisFragment'a geri dön
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
        
        // Anasayfaya dön butonuna tıklama olayını ayarla
        view.findViewById<Button>(R.id.btnBackToHomepage)?.setOnClickListener {
            // Ana sayfaya dön (geriye giderek)
            if (evaluationId != -1L) {
                // Eğer geçmiş değerlendirmeden gelindiyse, geçmiş sayfasına git
                findNavController().popBackStack(R.id.historyFragment, false)
            } else {
                // Normal akışta ana sayfaya dön
                findNavController().popBackStack(R.id.homePageFragment, false)
            }
        }
    }
    
    private fun displayAiNote() {
        if (evaluationResultJson != null) {
            try {
                // Değerlendirme sonucunu JSON olarak çözümle
                val resultJson = JSONObject(evaluationResultJson!!)
                val explanation = resultJson.getString("explanation")
                
                // Açıklama metnini göster
                binding.tvNoteContent.text = explanation
                
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvNoteContent.text = "Açıklama yüklenirken bir hata oluştu."
            }
        } else {
            binding.tvNoteContent.text = "Açıklama bulunamadı."
        }
    }

    // GestureDetector.OnGestureListener metodları
    override fun onDown(e: MotionEvent): Boolean = false
    
    override fun onShowPress(e: MotionEvent) {}
    
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    
    override fun onLongPress(e: MotionEvent) {}
    
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        // Sağa kaydırma hareketi algılandığında
        if (e1 != null && e2.x > e1.x && Math.abs(e1.x - e2.x) > 100 && Math.abs(velocityX) > 100) {
            // AnalysisFragment'a geri dön
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
         * @return A new instance of fragment AINoteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = AINoteFragment()
    }
}