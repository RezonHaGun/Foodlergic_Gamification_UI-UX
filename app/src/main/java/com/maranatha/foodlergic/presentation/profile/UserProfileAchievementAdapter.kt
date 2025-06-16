package com.maranatha.foodlergic.presentation.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.databinding.ItemAchievementBinding
import com.maranatha.foodlergic.databinding.ItemAchievementUserProfileBinding
import com.maranatha.foodlergic.domain.models.Achievement

class UserProfileAchievementAdapter :
    ListAdapter<Achievement, UserProfileAchievementAdapter.UserProfileAchievmentViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserProfileAchievmentViewHolder {
        val binding =
            ItemAchievementUserProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserProfileAchievmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserProfileAchievmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserProfileAchievmentViewHolder(private val binding: ItemAchievementUserProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
//            binding.achievementText.visibility = View.GONE
            //binding.achievementText.text = achievement.name
            val imageUrl = if (achievement.isUnlocked) {
                achievement.unlockedImage
            } else {
                achievement.lockedImage
            }

            Glide.with(binding.root.context)
                .load(imageUrl)
                .into(binding.achievementImage)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Achievement>() {
            override fun areItemsTheSame(
                oldItem: Achievement,
                newItem: Achievement
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: Achievement,
                newItem: Achievement
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}