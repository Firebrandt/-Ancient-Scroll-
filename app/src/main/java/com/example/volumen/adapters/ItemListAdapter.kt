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

    class ItemViewHolder(private val binding: ListItemBinding, private val onClick: (Article) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        // Assignments to views are handled via data binding in the layout.
        fun bind(itemToBind: Article) {
            binding.article = itemToBind
            binding.listCard.setOnClickListener {
                onClick(itemToBind)
            }
            // Immediately update any data bindings now that things have changed up...
            binding.executePendingBindings()

        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val thisItem = getItem(position)
        holder.bind(thisItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // REMINDER TO SELF: When inflating a view from a layoutInflater, we need to pass in parent to use layout_ attributes.
        // Those are part of the LayoutParams for a view. The view evaluates these in relation to the PARENT;
        // it uses values the parent gives to it to calculate its LayoutParams. Hence the need for it in the function.
        // No parent = THESE ARE THROWN OUT! (I had learned this before when curious, but forgot).

        val binding : ListItemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, updateViewModel)
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