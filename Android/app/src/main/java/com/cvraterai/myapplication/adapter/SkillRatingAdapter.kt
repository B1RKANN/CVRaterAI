package com.cvraterai.myapplication.adapter

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.R
import com.cvraterai.myapplication.model.SkillRating

class SkillRatingAdapter(private val skillRatings: List<SkillRating>) : 
    RecyclerView.Adapter<SkillRatingAdapter.SkillRatingViewHolder>() {

    // Animasyon durumunu izleyen değişken
    private var animationsEnabled = true
    
    class SkillRatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val skillName: TextView = view.findViewById(R.id.tvSkillName)
        val progressContainer: LinearLayout = view.findViewById(R.id.progressContainer)
        val progressFilled: View = view.findViewById(R.id.progressFilled)
        val ratingPercentage: TextView = view.findViewById(R.id.tvRatingPercentage)
        
        /**
         * Animasyon olmadan progress değerini ayarlar
         */
        fun setProgressWithoutAnimation(rating: Int) {
            val progressParams = progressFilled.layoutParams as LinearLayout.LayoutParams
            progressParams.weight = rating.toFloat()
            progressFilled.layoutParams = progressParams
            ratingPercentage.text = "%$rating"
        }
        
        /**
         * Progress değerini animasyonlu olarak ayarlar
         */
        fun setProgressWithAnimation(rating: Int, position: Int) {
            // Animasyon başlangıcında 0 weight ayarla
            val progressParams = progressFilled.layoutParams as LinearLayout.LayoutParams
            progressParams.weight = 0f
            progressFilled.layoutParams = progressParams
            
            // Yüzde metnini başlangıçta %0 olarak ayarla
            ratingPercentage.text = "%0"
            
            // Gecikme süresi hesaplama (pozisyona bağlı olarak kademeli başlat)
            val delayPerItem = 150L
            val startDelay = position * delayPerItem
            
            // Animasyon için ValueAnimator oluştur
            val animator = ValueAnimator.ofFloat(0f, rating.toFloat())
            animator.duration = 1000 // 1 saniye
            animator.startDelay = startDelay
            animator.interpolator = DecelerateInterpolator() // Yavaşlayan animasyon
            
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                
                // Progress bar'ı güncelle
                val params = progressFilled.layoutParams as LinearLayout.LayoutParams
                params.weight = animatedValue
                progressFilled.layoutParams = params
                
                // Yüzde metnini güncelle (int olarak göster)
                ratingPercentage.text = "%${animatedValue.toInt()}"
            }
            
            // Animasyonu başlat
            animator.start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillRatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skill_rating, parent, false)
        return SkillRatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkillRatingViewHolder, position: Int) {
        val skill = skillRatings[position]
        
        // Set skill name
        holder.skillName.text = skill.name
        
        try {
            // Animasyon seçimine göre progress bar'ı ayarla
            if (animationsEnabled) {
                holder.setProgressWithAnimation(skill.rating, position)
            } else {
                holder.setProgressWithoutAnimation(skill.rating)
            }
        } catch (e: Exception) {
            android.util.Log.e("SkillRatingAdapter", "onBindViewHolder error: ${e.message}")
            // Hata durumunda animasyonsuz görüntüle
            holder.setProgressWithoutAnimation(skill.rating)
        }
    }

    override fun getItemCount() = skillRatings.size
    
    /**
     * Animasyonları kapatmak için kullanılabilir
     * (örn. RecyclerView'de onScrolled sırasında gereksiz animasyonları önlemek için)
     */
    fun disableAnimations() {
        animationsEnabled = false
    }
} 