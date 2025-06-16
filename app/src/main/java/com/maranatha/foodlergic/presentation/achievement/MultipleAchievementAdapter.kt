package com.maranatha.foodlergic.presentation.achievement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.databinding.LinearItemAchievementBinding
import com.maranatha.foodlergic.domain.models.Achievement

class MultipleAchievementAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<MultipleAchievementAdapter.MultipleAchievementViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultipleAchievementViewHolder {
        val binding =
            LinearItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MultipleAchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MultipleAchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.bind(achievement)
    }

    override fun getItemCount() = achievements.size

    inner class MultipleAchievementViewHolder(private val binding: LinearItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(achievement: Achievement) {
            Glide.with(binding.root.context)
                .load(achievement.unlockedImage)
                .into(binding.ivAchievement)
            binding.tvAchievementTitle.text = achievement.name
            binding.tvAchievementDescription.text = achievement.description
        }
    }
}