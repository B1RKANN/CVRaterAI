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
 * Use the [Step2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Step2Fragment : Fragment(), GestureDetector.OnGestureListener {
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
        val view = inflater.inflate(R.layout.fragment_step2, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Gesture detector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        
        // Ana layout'a dokunma olaylarını dinle
        val mainLayout = view.findViewById<View>(R.id.step2_layout)
        mainLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
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
            // Step3Fragment'a geçiş yap
            findNavController().navigate(R.id.action_step2Fragment_to_step3Fragment)
            return true
        }
        // Sağa kaydırma hareketi algılandığında
        else if (e1 != null && e2.x > e1.x && Math.abs(e1.x - e2.x) > 100 && Math.abs(velocityX) > 100) {
            // Step1Fragment'a geri dön
            findNavController().navigate(R.id.action_step2Fragment_to_step1Fragment)
            return true
        }
        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Step2Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Step2Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}