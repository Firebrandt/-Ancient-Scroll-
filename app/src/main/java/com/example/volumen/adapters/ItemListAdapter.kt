package com.example.volumen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.volumen.R
import com.example.volumen.data.Article
import com.example.volumen.databinding.ListItemBinding

class ItemListAdapter(val updateViewModel: (Article) -> Unit) : ListAdapter<Article, ItemListAdapter.ItemViewHolder>(DiffCallback) {
    /** A fairly standard ListAdapter meant to serve the itemList recycler view.
     * updateViewModel is passed in to be used as an onClickListener for view model updating with
     * a clicked article.
     *
     * **/

    class ItemViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // Assignments to views are handled via data binding in the layout.
        fun bind(itemToBind: Article) {
            binding.article = itemToBind
            // Immediately update any data bindings now that things have changed up...
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val thisItem = getItem(position)
        holder.bind(thisItem)
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