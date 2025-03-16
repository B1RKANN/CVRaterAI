package com.cvraterai.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cvraterai.myapplication.R
import com.cvraterai.myapplication.model.SkillRating

class SkillRatingAdapter(private val skillRatings: List<SkillRating>) : 
    RecyclerView.Adapter<SkillRatingAdapter.SkillRatingViewHolder>() {

    class SkillRatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val skillName: TextView = view.findViewById(R.id.tvSkillName)
        val progressContainer: LinearLayout = view.findViewById(R.id.progressContainer)
        val progressFilled: View = view.findViewById(R.id.progressFilled)
        val ratingPercentage: TextView = view.findViewById(R.id.tvRatingPercentage)
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
        
        // Set progress bar weights
        val progressParams = holder.progressFilled.layoutParams as LinearLayout.LayoutParams
        progressParams.weight = skill.rating.toFloat() 
        holder.progressFilled.layoutParams = progressParams
        
        // Set percentage text
        holder.ratingPercentage.text = "%${skill.rating}"
    }

    override fun getItemCount() = skillRatings.size
} 