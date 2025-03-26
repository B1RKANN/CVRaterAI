package com.cvraterai.myapplication.adapter

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.R
import com.cvraterai.myapplication.model.SkillRating

class SkillRatingAdapter(private var skillRatings: List<SkillRating>) : 
    RecyclerView.Adapter<SkillRatingAdapter.SkillViewHolder>() {

    // Animasyon durumunu izleyen değişken
    private var animationsEnabled = true
    
    // Renk sabitleri
    private val COLOR_RED = Color.parseColor("#FF4B4B")
    private val COLOR_ORANGE = Color.parseColor("#FFA726")
    private val COLOR_GREEN = Color.parseColor("#4CAF50")
    
    class SkillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSkillName: TextView = view.findViewById(R.id.tvSkillName)
        val progressFilled: View = view.findViewById(R.id.progressFilled)
        val progressContainer: FrameLayout = view.findViewById(R.id.progressContainer)
        val tvRatingPercentage: TextView = view.findViewById(R.id.tvRatingPercentage)
        
        /**
         * Animasyon olmadan progress değerini ayarlar
         */
        fun setProgressWithoutAnimation(percentage: Int, adapter: SkillRatingAdapter) {
            updateProgressWidth(percentage)
            tvRatingPercentage.text = "%$percentage"
            adapter.updateProgressColor(progressFilled, percentage, false)
        }
        
        /**
         * Progress değerini animasyonlu olarak ayarlar
         */
        fun setProgressWithAnimation(percentage: Int, position: Int, adapter: SkillRatingAdapter) {
            // Başlangıçta 0 genişlik
            updateProgressWidth(0)
            tvRatingPercentage.text = "%0"
            
            // Gecikme süresi hesaplama (pozisyona bağlı olarak kademeli başlat)
            val delayPerItem = 150L
            val startDelay = position * delayPerItem
            
            // Animasyon için ValueAnimator oluştur
            val animator = ValueAnimator.ofFloat(0f, percentage.toFloat())
            animator.duration = 1000 // 1 saniye
            animator.startDelay = startDelay
            animator.interpolator = DecelerateInterpolator() // Yavaşlayan animasyon
            
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val currentPercentage = animatedValue.toInt()
                updateProgressWidth(currentPercentage)
                tvRatingPercentage.text = "%$currentPercentage"
                adapter.updateProgressColor(progressFilled, currentPercentage, true)
            }
            
            // Animasyonu başlat
            animator.start()
        }

        private fun updateProgressWidth(percentage: Int) {
            val containerWidth = progressContainer.width
            val newWidth = (containerWidth * (percentage / 100f)).toInt()
            val params = progressFilled.layoutParams
            params.width = newWidth
            progressFilled.layoutParams = params
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill_rating, parent, false)
        return SkillViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = skillRatings[position]
        holder.tvSkillName.text = skill.name
        
        try {
            // Animasyon seçimine göre progress bar'ı ayarla
            if (animationsEnabled) {
                holder.setProgressWithAnimation(skill.percentage, position, this)
            } else {
                holder.setProgressWithoutAnimation(skill.percentage, this)
            }
            
            // Container'ın genişliği değiştiğinde progress bar'ı güncelle
            holder.progressContainer.post {
                if (animationsEnabled) {
                    holder.setProgressWithAnimation(skill.percentage, position, this)
                } else {
                    holder.setProgressWithoutAnimation(skill.percentage, this)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SkillRatingAdapter", "onBindViewHolder error: ${e.message}")
            // Hata durumunda animasyonsuz görüntüle
            holder.setProgressWithoutAnimation(skill.percentage, this)
        }
    }

    override fun getItemCount(): Int = skillRatings.size
    
    /**
     * Animasyonları kapatmak için kullanılabilir
     * (örn. RecyclerView'de onScrolled sırasında gereksiz animasyonları önlemek için)
     */
    fun disableAnimations() {
        animationsEnabled = false
    }

    fun updateSkills(newSkills: List<SkillRating>) {
        skillRatings = newSkills
        notifyDataSetChanged()
    }

    private fun updateProgressColor(progressFilled: View, percentage: Int, animate: Boolean) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = progressFilled.context.resources.getDimension(R.dimen.progress_corner_radius)

        if (animate) {
            // Yüzdeye göre renk geçişi
            val targetColor = when {
                percentage < 50 -> COLOR_RED
                percentage < 70 -> COLOR_ORANGE
                else -> COLOR_GREEN
            }

            val currentColor = when {
                percentage < 25 -> COLOR_RED
                percentage < 50 -> interpolateColor(COLOR_RED, COLOR_ORANGE, (percentage - 25) / 25f)
                percentage < 70 -> interpolateColor(COLOR_ORANGE, COLOR_GREEN, (percentage - 50) / 20f)
                else -> COLOR_GREEN
            }

            drawable.setColor(currentColor)
        } else {
            // Animasyonsuz direkt renk ataması
            val color = when {
                percentage < 50 -> COLOR_RED
                percentage < 70 -> COLOR_ORANGE
                else -> COLOR_GREEN
            }
            drawable.setColor(color)
        }

        progressFilled.background = drawable
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        return ArgbEvaluator().evaluate(fraction, startColor, endColor) as Int
    }
} 