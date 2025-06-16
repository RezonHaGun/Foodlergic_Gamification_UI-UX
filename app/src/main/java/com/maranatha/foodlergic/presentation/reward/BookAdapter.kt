package com.maranatha.foodlergic.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maranatha.foodlergic.databinding.ItemBookBinding
import com.maranatha.foodlergic.domain.models.Book

class BookAdapter : ListAdapter<Book, BookAdapter.BookViewHolder>(DIFF_CALLBACK) {
    class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
//            binding.bookTitle.text = book.name
//            binding.summaryText.text = book.summary

            Glide.with(binding.root.context)
                .load(book.image)
                .into(binding.imageBook)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
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
