package com.maranatha.foodlergic.presentation.allergic

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.data.models.Allergy
import com.maranatha.foodlergic.databinding.ItemAllergyBinding

class AllergyAdapter(
) : ListAdapter<Allergy, AllergyAdapter.AllergyViewHolder>(DIFF_CALLBACK) {

    inner class AllergyViewHolder(private val binding: ItemAllergyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Allergy) {
            binding.allergyImage.setImageResource(item.image)
            binding.allergyName.text = item.name

            binding.allergyCard.setCardBackgroundColor(
                if (item.isSelected) ContextCompat.getColor(
                    itemView.context,
                    R.color.custom_color_secondary_light
                )
                else Color.WHITE
            )

            binding.root.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergyViewHolder {
        val binding = ItemAllergyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllergyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllergyViewHolder, position: Int) {
        holder.bind(getItem(position))
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
