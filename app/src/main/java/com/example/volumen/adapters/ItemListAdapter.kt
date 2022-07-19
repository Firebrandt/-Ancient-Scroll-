package com.example.volumen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.volumen.R
import com.example.volumen.data.Article
import com.example.volumen.databinding.ListItemBinding

class ItemListAdapter() : ListAdapter<Article, ItemListAdapter.ItemViewHolder>(DiffCallback) {
    /** A fairly standard ListAdapter meant to serve the itemList recycler view. **/

    class ItemViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val dateTextView = binding.eventDateText
        val titleTextView = binding.eventTitleText
        val eventImageView = binding.eventImage
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val thisItem = getItem(position)
        holder.dateTextView.text = thisItem.date
        holder.titleTextView.text = thisItem.title

        // TODO: Implement actually loading an image from the internet.
        // If we have images, use the first one as the list item image.
        holder.eventImageView.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding : ListItemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context))
        return ItemViewHolder(binding)
    }

    companion object {

        val DiffCallback = object : DiffUtil.ItemCallback<Article>() {

            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }
        }
    }

}