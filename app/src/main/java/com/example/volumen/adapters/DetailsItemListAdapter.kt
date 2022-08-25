package com.example.volumen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.volumen.R


class DetailsItemListAdapter(private val imageList: List<String>) : RecyclerView.Adapter<DetailsItemListAdapter.DetailsImageViewHolder>() {
    /** A fairly standard Recycler View adapter for the details item list, in the details pane,
     * that shows article images in a carousel-like display.
     *
     * Instance attributes:
     * - imageList: The list of image URLs we plan to load in.
     */

    class DetailsImageViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView? = view.findViewById(R.id.image_list_image)
        val text: TextView? = view.findViewById(R.id.image_caption)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsImageViewHolder {
        return DetailsImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: DetailsImageViewHolder, position: Int) {
        val item = imageList[position]
        holder.imageView?.load(item)

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

}