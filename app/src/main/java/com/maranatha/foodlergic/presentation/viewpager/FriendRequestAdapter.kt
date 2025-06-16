package com.maranatha.foodlergic.presentation.viewpager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.databinding.ItemFriendRequestBinding
import com.maranatha.foodlergic.domain.models.FriendRequest

class FriendRequestAdapter(private val listener: OnFriendRequestActionListener) : ListAdapter<FriendRequest, FriendRequestAdapter.FriendRequestViewHolder>(FriendRequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val request = getItem(position)
        holder.bind(request)
    }

    inner class FriendRequestViewHolder(private val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(request: FriendRequest) {
            binding.nameFriendRequest.text = request.fromUserName
            binding.idFriendRequest.text = "ID: ${request.fromFriendCode}"

            binding.buttonAccept.setOnClickListener {
                listener.onAccept(request)
            }

            binding.buttonDecline.setOnClickListener {
                listener.onDecline(request)
            }
        }
    }

    class FriendRequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
        override fun areItemsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FriendRequest, newItem: FriendRequest): Boolean {
            return oldItem == newItem
        }
    }

    interface OnFriendRequestActionListener {
        fun onAccept(request: FriendRequest)
        fun onDecline(request: FriendRequest)
    }
}
