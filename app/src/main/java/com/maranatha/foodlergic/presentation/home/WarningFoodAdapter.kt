package com.maranatha.foodlergic.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.databinding.ItemFoodWarningBinding
import com.maranatha.foodlergic.domain.models.WarningFood

class WarningFoodAdapter :
    ListAdapter<WarningFood, WarningFoodAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemFoodWarningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WarningFood) {
            val (food, count) = item
            binding.tvFoodName.text = "🍽 ${item.foodName}"
            binding.tvDetails.text = "Scanned ${item.scanCount} times (allergen: ${item.allergen})"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodWarningBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<WarningFood>() {

        override fun areItemsTheSame(oldItem: WarningFood, newItem: WarningFood): Boolean {
            return oldItem.foodName == newItem.foodName
        }

        override fun areContentsTheSame(oldItem: WarningFood, newItem: WarningFood): Boolean {
            return oldItem == newItem
        }
    }
}
