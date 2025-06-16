package com.maranatha.foodlergic.presentation.leaderboard


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(private val onItemClick: (LeaderboardItem) -> Unit) :
    ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderboardViewHolder>(DIFF_CALLBACK) {

    inner class LeaderboardViewHolder(
        private val binding: ItemLeaderboardBinding,
        onItemClicked: (LeaderboardItem) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val item = getItem(bindingAdapterPosition)
                onItemClicked(item) // Trigger listener saat item diklik
            }
        }

        fun bind(item: LeaderboardItem) {
            binding.rankTextView.text = "${item.rank}."
            binding.playerNameTextView.text = item.playerName
            binding.scoreTextView.text = item.score.toString()
            binding.levelTextView.text = item.level
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding =
            ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LeaderboardViewHolder(binding) {
            onItemClick(it)
        }
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<LeaderboardItem>() {
            override fun areItemsTheSame(
                oldItem: LeaderboardItem,
                newItem: LeaderboardItem
            ): Boolean {
                return oldItem.rank == newItem.rank
            }

            override fun areContentsTheSame(
                oldItem: LeaderboardItem,
                newItem: LeaderboardItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}



