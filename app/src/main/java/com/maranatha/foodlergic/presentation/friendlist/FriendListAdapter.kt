package com.maranatha.foodlergic.presentation.friendlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maranatha.foodlergic.R

class FriendListAdapter(private val context: Context, private val friendsList: List<String>) :
    RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_friend_list, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.rankTextView.text = "${position + 1}."
        holder.playerNameTextView.text = friendsList[position]
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerNameTextView: TextView = itemView.findViewById(R.id.playerNameTextView)
        val rankTextView: TextView = itemView.findViewById(R.id.rankTextView) // ini harus ada
    }
}
