package com.maranatha.foodlergic.presentation.reward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.R
import com.maranatha.foodlergic.domain.models.Book

class RewardAdapter() :
    ListAdapter<Book,RewardAdapter.RewardViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((Book) -> Unit)? = null

    class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBook: ImageView = itemView.findViewById(R.id.imageBook)
//        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
//        val summaryText: TextView = itemView.findViewById(R.id.summaryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_cardview, parent, false)
        return RewardViewHolder(view)
    }


    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val book = getItem(position)
        Glide.with(holder.itemView.context)
            .load(book.image)  // Menggunakan URL gambar jika tersedia
            .into(holder.imageBook)

//        holder.bookTitle.text = book.name
//        holder.summaryText.text = book.summary

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book) // Mengirim data buku yang diklik
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(
                oldItem: Book,
                newItem: Book
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: Book,
                newItem: Book
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
