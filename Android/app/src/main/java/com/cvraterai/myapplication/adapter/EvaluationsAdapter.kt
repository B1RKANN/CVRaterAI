package com.cvraterai.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.R
import com.cvraterai.myapplication.data.model.CvEvaluationResponse
import java.text.SimpleDateFormat
import java.util.Locale

class EvaluationsAdapter(
    private var evaluations: List<CvEvaluationResponse> = emptyList(),
    private val onItemClick: (CvEvaluationResponse) -> Unit
) : RecyclerView.Adapter<EvaluationsAdapter.EvaluationViewHolder>() {

    class EvaluationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvaluationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evaluation, parent, false)
        return EvaluationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EvaluationViewHolder, position: Int) {
        val evaluation = evaluations[position]
        
        // Ad soyad
        holder.tvFullName.text = evaluation.fullName
        
        // Değerlendirme tarihi
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = try {
            val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val parsedDate = apiDateFormat.parse(evaluation.evaluationDate)
            dateFormat.format(parsedDate ?: "")
        } catch (e: Exception) {
            evaluation.evaluationDate
        }

        
        // Dosya adı

        
        // Değerlendirme puanı - yüzde işareti olmadan

        
        // Detay butonuna tıklama
        holder.btnViewDetails.setOnClickListener {
            onItemClick(evaluation)
        }
    }

    override fun getItemCount(): Int = evaluations.size

    fun updateData(newEvaluations: List<CvEvaluationResponse>) {
        this.evaluations = newEvaluations
        notifyDataSetChanged()
    }
} 