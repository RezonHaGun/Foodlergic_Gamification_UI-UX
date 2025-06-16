package com.maranatha.foodlergic.presentation.reward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.databinding.LinearItemAchievementBinding
import com.maranatha.foodlergic.domain.models.Achievement
import com.maranatha.foodlergic.domain.models.Book

class MultipleRewardAdapter(private val rewards: List<Book>) :
    RecyclerView.Adapter<MultipleRewardAdapter.MultipleRewardViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MultipleRewardViewHolder {
        val binding =
            LinearItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MultipleRewardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MultipleRewardViewHolder, position: Int) {
        val achievement = rewards[position]
        holder.bind(achievement)
    }

    override fun getItemCount() = rewards.size

    inner class MultipleRewardViewHolder(private val binding: LinearItemAchievementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            Glide.with(binding.root.context)
                .load(book.image)
                .into(binding.ivAchievement)
            binding.tvAchievementTitle.text = book.name
            binding.tvAchievementDescription.visibility = View.GONE
        }
    }
}