package com.maranatha.foodlergic.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.databinding.ItemFoodScanHistoryBinding
import com.maranatha.foodlergic.domain.models.Predict

class FoodScanHistoryAdapter :
    ListAdapter<Predict, FoodScanHistoryAdapter.FoodScanHistoryViewHolder>(DIFF_CALLBACK) {

    inner class FoodScanHistoryViewHolder(private val binding: ItemFoodScanHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Predict) {
            if (item.hasAllergy) {
                binding.tvAllergydesc.text = "Allergic"
                binding.tvAllergydesc.setTextColor(ContextCompat.getColor(binding.root.context, R.color.color_button_red))
                binding.tvPoint.text = "+ 0 Point"
            } else {
                binding.tvAllergydesc.text = "Safe"
                binding.tvAllergydesc.setTextColor(ContextCompat.getColor(binding.root.context, R.color.custom_color_secondary_light))
                binding.tvPoint.text = "+ 5 Point"
            }
            binding.tvAllergyLabel.text = item.predictedAllergen
            binding.tvfoodname.text = item.foodName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodScanHistoryViewHolder {
        val binding =
            ItemFoodScanHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodScanHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodScanHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Predict>() {
            override fun areItemsTheSame(
                oldItem: Predict,
                newItem: Predict
            ): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(
                oldItem: Predict,
                newItem: Predict
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}