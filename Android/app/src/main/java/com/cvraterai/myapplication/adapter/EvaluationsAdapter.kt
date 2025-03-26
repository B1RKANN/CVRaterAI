package com.cvraterai.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.R
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Değerlendirme listesini tarih kategorileriyle gösteren adapter
 */
class EvaluationsAdapter(private val onItemClick: (evaluation: CvEvaluationResponse) -> Unit) : 
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    // View tipleri
    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1
    
    // Kategorize edilmiş veri yapısı
    private val items = ArrayList<Any>()
    
    /**
     * Değerlendirmeleri tarih kategorilerine göre gruplayıp adapter'a ekler
     */
    fun updateData(evaluations: List<CvEvaluationResponse>) {
        items.clear()
        
        if (evaluations.isEmpty()) return
        
        // Değerlendirmeleri tarihe göre sırala (en yeniden eskiye)
        val sortedEvaluations = evaluations.sortedByDescending { it.getDateTimestamp() }
        
        // Tarih kategorilerine göre gruplay
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        yesterday.set(Calendar.HOUR_OF_DAY, 0)
        yesterday.set(Calendar.MINUTE, 0)
        yesterday.set(Calendar.SECOND, 0)
        yesterday.set(Calendar.MILLISECOND, 0)
        
        val thisWeekStart = Calendar.getInstance()
        thisWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        thisWeekStart.set(Calendar.HOUR_OF_DAY, 0)
        thisWeekStart.set(Calendar.MINUTE, 0)
        thisWeekStart.set(Calendar.SECOND, 0)
        thisWeekStart.set(Calendar.MILLISECOND, 0)
        
        val thisMonthStart = Calendar.getInstance()
        thisMonthStart.set(Calendar.DAY_OF_MONTH, 1)
        thisMonthStart.set(Calendar.HOUR_OF_DAY, 0)
        thisMonthStart.set(Calendar.MINUTE, 0)
        thisMonthStart.set(Calendar.SECOND, 0)
        thisMonthStart.set(Calendar.MILLISECOND, 0)
        
        // Değerlendirmeleri kategorilere ayır
        val todayItems = ArrayList<CvEvaluationResponse>()
        val yesterdayItems = ArrayList<CvEvaluationResponse>()
        val thisWeekItems = ArrayList<CvEvaluationResponse>()
        val thisMonthItems = ArrayList<CvEvaluationResponse>()
        val olderItems = ArrayList<CvEvaluationResponse>()
        
        for (evaluation in sortedEvaluations) {
            val evaluationDate = Calendar.getInstance()
            evaluationDate.timeInMillis = evaluation.getDateTimestamp()
            
            when {
                evaluationDate.after(today) || isSameDay(evaluationDate, today) -> {
                    todayItems.add(evaluation)
                }
                evaluationDate.after(yesterday) || isSameDay(evaluationDate, yesterday) -> {
                    yesterdayItems.add(evaluation)
                }
                evaluationDate.after(thisWeekStart) -> {
                    thisWeekItems.add(evaluation)
                }
                evaluationDate.after(thisMonthStart) -> {
                    thisMonthItems.add(evaluation)
                }
                else -> {
                    olderItems.add(evaluation)
                }
            }
        }
        
        // Kategorilere göre listeyi oluştur
        if (todayItems.isNotEmpty()) {
            items.add(DateHeader(R.string.today))
            items.addAll(todayItems)
        }
        
        if (yesterdayItems.isNotEmpty()) {
            items.add(DateHeader(R.string.yesterday))
            items.addAll(yesterdayItems)
        }
        
        if (thisWeekItems.isNotEmpty()) {
            items.add(DateHeader(R.string.this_week))
            items.addAll(thisWeekItems)
        }
        
        if (thisMonthItems.isNotEmpty()) {
            items.add(DateHeader(R.string.this_month))
            items.addAll(thisMonthItems)
        }
        
        if (olderItems.isNotEmpty()) {
            items.add(DateHeader(R.string.older))
            items.addAll(olderItems)
        }
        
        notifyDataSetChanged()
    }
    
    // İki tarihin aynı gün olup olmadığını kontrol eder
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (items[position] is DateHeader) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_evaluation, parent, false)
                EvaluationViewHolder(view, onItemClick)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as DateHeader
                holder.bind(header)
            }
            is EvaluationViewHolder -> {
                val evaluation = items[position] as CvEvaluationResponse
                holder.bind(evaluation)
            }
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    // Kategori başlığı için ViewHolder
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryTitle: TextView = itemView.findViewById(R.id.tvCategoryTitle)
        
        fun bind(header: DateHeader) {
            tvCategoryTitle.setText(header.titleResId)
        }
    }
    
    // Değerlendirme öğesi için ViewHolder
    inner class EvaluationViewHolder(itemView: View, private val onItemClick: (evaluation: CvEvaluationResponse) -> Unit) : 
        RecyclerView.ViewHolder(itemView) {
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnViewDetails: CardView = itemView.findViewById(R.id.btnViewDetails)
        
        fun bind(evaluation: CvEvaluationResponse) {
            // Full name veya file name gösterimi
            if (evaluation.fullName.isNullOrEmpty()) {
                tvFullName.text = evaluation.fileName ?: "Unknown"
            } else {
                tvFullName.text = evaluation.fullName
            }
            
            // Tarih formatını ayarla ve göster
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00'", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = evaluation.date?.let { dateFormat.parse(it) }
                
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.getDefault())
                    tvDate.text = outputFormat.format(date)
                } else {
                    tvDate.text = evaluation.date ?: ""
                }
            } catch (e: Exception) {
                tvDate.text = evaluation.date ?: ""
            }

            // Tıklama olayını CardView'a ekle
            btnViewDetails.setOnClickListener {
                onItemClick(evaluation)
            }
        }
    }
    
    // Tarih kategorisi için veri sınıfı
    data class DateHeader(val titleResId: Int)

    private fun updateProgressBar(progressFilled: View, percentage: Int) {
        val params = progressFilled.layoutParams as LinearLayout.LayoutParams
        params.weight = percentage.toFloat()
        progressFilled.layoutParams = params

        // Yüzdeye göre renk değişimi
        when {
            percentage < 50 -> {
                progressFilled.setBackgroundResource(R.drawable.rounded_progress_red)
            }
            percentage < 70 -> {
                progressFilled.setBackgroundResource(R.drawable.rounded_progress_orange)
            }
            else -> {
                progressFilled.setBackgroundResource(R.drawable.rounded_progress_green)
            }
        }
    }
} 