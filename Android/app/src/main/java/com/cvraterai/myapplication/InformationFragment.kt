package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController
import com.cvraterai.myapplication.databinding.FragmentInformationBinding
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InformationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InformationFragment : Fragment(), GestureDetector.OnGestureListener {
    private var _binding: FragmentInformationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var gestureDetector: GestureDetectorCompat
    
    // API yanıtını tutacak değişkenler
    private var evaluationResponse: String? = null
    private var evaluationResultJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Argümanları al
        arguments?.let {
            evaluationResponse = it.getString("evaluationResponse")
            evaluationResultJson = it.getString("evaluationResultJson")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // View binding kullanarak inflate et
        _binding = FragmentInformationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Verileri göster
        displayUserInfo()
        
        // Gesture detector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        
        // Ana layout'a dokunma olaylarını dinle
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        
        // Anasayfaya dön butonuna tıklama olayını ayarla
        view.findViewById<Button>(R.id.btnBackToHomepage)?.setOnClickListener {
            // Anasayfaya geçiş yap
            findNavController().navigate(R.id.action_informationFragment_to_homePageFragment)
        }
    }
    
    private fun displayUserInfo() {
        if (evaluationResultJson != null) {
            try {
                // Değerlendirme sonucunu JSON olarak çözümle
                val resultJson = JSONObject(evaluationResultJson!!)
                val userInfo = resultJson.getJSONObject("userInformation")
                
                // Kullanıcı bilgilerini view'lara ekle
                binding.name.text = userInfo.getString("name")
                binding.surname.text = userInfo.getString("surname")
                binding.email.text = userInfo.getString("email")
                binding.phonenumber.text = userInfo.getString("phone")
                binding.skills.text = userInfo.getString("skills")
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
            // AnalysisFragment'a geçiş yap ve veriyi aktar
            val bundle = bundleOf(
                "evaluationResponse" to evaluationResponse,
                "evaluationResultJson" to evaluationResultJson
            )
            
            findNavController().navigate(R.id.action_informationFragment_to_analysisFragment, bundle)
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
         * @return A new instance of fragment InformationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InformationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}