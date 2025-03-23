package com.cvraterai.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Özelleştirilmiş bir ProgressBar sınıfı
 * 
 * Animasyonlu ve animasyonsuz ilerleme göstergesini yönetir
 */
class ProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var progressView: View? = null
    private var maxProgress = 100
    private var currentProgress = 0
    
    private var animator: ValueAnimator? = null
    private var isAnimationEnabled = true
    
    init {
        // Inflate XML layout
        inflate(context, R.layout.view_progress_bar, this)
        
        // Get the progress view
        progressView = findViewById(R.id.progressFilled)
    }
    
    /**
     * Progress değerini animasyonlu olarak ayarlar
     */
    fun startAnimation(targetProgress: Int) {
        // Animasyon zaten çalışıyorsa durdur
        animator?.cancel()
        
        // Hedef değeri sınırla
        val boundedProgress = targetProgress.coerceIn(0, maxProgress)
        
        // Animasyon etkin değilse, direkt değeri ayarla
        if (!isAnimationEnabled) {
            setProgressWithoutAnimation(boundedProgress)
            return
        }
        
        // Başlangıç genişliğini mevcut ilerlemeye göre ayarla
        val startWidth = (width * currentProgress) / maxProgress
        val endWidth = (width * boundedProgress) / maxProgress
        
        // Animasyon oluştur
        animator = ValueAnimator.ofInt(startWidth, endWidth).apply {
            duration = 1000 // 1 saniye
            interpolator = DecelerateInterpolator()
            
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                
                // Progress view genişliğini güncelle
                progressView?.layoutParams?.width = animatedValue
                progressView?.requestLayout()
                
                // Mevcut ilerlemeyi oransal olarak güncelle
                currentProgress = if (width > 0) (animatedValue * maxProgress) / width else 0
            }
            
            start()
        }
    }
    
    /**
     * Progress değerini animasyonsuz olarak ayarlar
     */
    fun setProgressWithoutAnimation(progress: Int) {
        // Animasyon çalışıyorsa durdur
        animator?.cancel()
        
        // Değeri sınırla
        currentProgress = progress.coerceIn(0, maxProgress)
        
        // Genişliği direkt güncelle
        val targetWidth = (width * currentProgress) / maxProgress
        progressView?.layoutParams?.width = targetWidth
        progressView?.requestLayout()
    }
    
    /**
     * Animasyonları devre dışı bırakır
     */
    fun disableAnimations() {
        isAnimationEnabled = false
    }
    
    /**
     * Animasyonları etkinleştirir
     */
    fun enableAnimations() {
        isAnimationEnabled = true
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Boyut değiştiğinde, progress view genişliğini güncelle
        val targetWidth = (w * currentProgress) / maxProgress
        progressView?.layoutParams?.width = targetWidth
        progressView?.requestLayout()
    }
} 