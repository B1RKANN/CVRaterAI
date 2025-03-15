package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    
    private lateinit var gestureDetector: GestureDetectorCompat

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analysis, container, false)
        
        // Kaydırma hareketlerini algılamak için GestureDetector oluştur
        setupSwipeGesture(view)
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize UI elements and set up any necessary listeners
        setupUI()
    }
    
    private fun setupSwipeGesture(view: View) {
        // SimpleOnGestureListener kullanarak kaydırma hareketlerini algıla
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            // Dokunma olayını algıla
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                try {
                    // Yatay kaydırma mesafesi ve hızı kontrol et
                    if (e1 != null) {
                        val diffX = e2.x - e1.x
                        val diffY = e2.y - e1.y
                        
                        // Yatay kaydırma dikey kaydırmadan daha belirginse ve yeterince hızlıysa
                        if (Math.abs(diffX) > Math.abs(diffY) && 
                            Math.abs(diffX) > 100 && 
                            Math.abs(velocityX) > 100) {
                            
                            // Soldan sağa kaydırma (geri gitme)
                            if (diffX > 0) {
                                // InformationFragment'a geri dön
                                findNavController().popBackStack()
                                return true
                            }
                        }
                    }
                } catch (exception: Exception) {
                    // Herhangi bir hata durumunda çökmeyi önle
                }
                return false
            }
        }
        
        // GestureDetector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), gestureListener)
        
        // Tüm view'a dokunma olaylarını dinle
        view.setOnTouchListener { _, event ->
            // Gesture detector'a gönder
            gestureDetector.onTouchEvent(event)
            true // Olayı tükettiğimizi belirtmek için true döndürüyoruz
        }
    }
    
    private fun setupUI() {
        // Here you would initialize any dynamic elements or set up click listeners
        // For this static UI, we don't need to do anything special
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