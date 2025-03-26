package com.cvraterai.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController
import android.widget.LinearLayout
import com.cvraterai.myapplication.databinding.FragmentStep1Binding
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.animation.AlphaAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.AnimationSet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Step1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Step1Fragment : Fragment(), GestureDetector.OnGestureListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var swipeAnimation: ImageView
    
    private var _binding: FragmentStep1Binding? = null
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
    ): View {
        _binding = FragmentStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Gesture detector'ı başlat
        gestureDetector = GestureDetectorCompat(requireContext(), this)
        
        // Swipe animasyonu için ImageView'ı bul
        swipeAnimation = view.findViewById(R.id.swipeAnimation)
        
        // Swipe animasyonunu başlat
        startSwipeAnimation()
        
        // Baloncuk animasyonlarını başlat
        startBubbleAnimations()
        
        // Ana layout'a dokunma olaylarını dinle
        val mainLayout = view.findViewById<View>(R.id.step1_layout)
        mainLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun startSwipeAnimation() {
        // Sağa kaydırma animasyonunu yükle
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        animation.repeatMode = android.view.animation.Animation.REVERSE
        animation.repeatCount = android.view.animation.Animation.INFINITE
        swipeAnimation.startAnimation(animation)
    }

    private fun startBubbleAnimations() {
        val bubbles = listOf(
            binding.bubble1Step1,
            binding.bubble2Step1,
            binding.bubble3Step1,
            binding.bubble4Step1,
            binding.bubble5Step1,
            binding.bubble6Step1,
            binding.bubble7Step1,
            binding.bubble8Step1,
            binding.bubble9Step1,
            binding.bubble10Step1,
            binding.bubble11Step1,
            binding.bubble12Step1,
            binding.bubble13Step1,
            binding.bubble14Step1,
            binding.bubble15Step1
        )

        // Tüm baloncukları animasyonlarla hareketlendirme
        bubbles.forEachIndexed { index, bubble ->
            // Her baloncuk için özel ve random değerler hesapla - mesafeleri arttırdım
            val baseDistance = 1.0f + (Math.random() * 1.2f).toFloat() // 1.0 ile 2.2 arası
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

    // GestureDetector.OnGestureListener metodları
    override fun onDown(e: MotionEvent): Boolean = false
    
    override fun onShowPress(e: MotionEvent) {}
    
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    
    override fun onLongPress(e: MotionEvent) {}
    
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        // Sola kaydırma hareketi algılandığında
        if (e1 != null && e2.x < e1.x && Math.abs(e1.x - e2.x) > 100 && Math.abs(velocityX) > 100) {
            // Step2Fragment'a geçiş yap
            findNavController().navigate(R.id.action_step1Fragment_to_step2Fragment)
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
         * @return A new instance of fragment Step1Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Step1Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}