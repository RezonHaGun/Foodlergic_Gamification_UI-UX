package com.maranatha.foodlergic.presentation.profile


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.models.Allergy
import com.maranatha.foodlergic.databinding.ItemUserAllergyBinding

class UserProfileAllergicAdapter :
    ListAdapter<Allergy, UserProfileAllergicAdapter.UserProfileAchievmentViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserProfileAchievmentViewHolder {
        val binding =
            ItemUserAllergyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserProfileAchievmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserProfileAchievmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserProfileAchievmentViewHolder(private val binding: ItemUserAllergyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(allergy: Allergy) {
            binding.allergyName.text = allergy.name
            binding.allergyImage.setImageResource(allergy.image)
            val context = itemView.context
            if (allergy.isSelected) {
                binding.allergyImage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.color_background
                    )
                )
            } else {
                binding.allergyImage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Allergy>() {
            override fun areItemsTheSame(
                oldItem: Allergy,
                newItem: Allergy
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: Allergy,
                newItem: Allergy
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}