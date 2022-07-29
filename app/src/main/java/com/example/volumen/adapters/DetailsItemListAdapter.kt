package com.example.volumen.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.volumen.R

class DetailsItemListAdapter(val imageList: List<String>) : RecyclerView.Adapter<DetailsItemListAdapter.DetailsImageViewHolder>() {
    /** A fairly standard Recycler View adapter for the details item list, in the details pane,
     * that shows article images in a carousel-like display.
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
        // TODO: Is this way better or is the binding adapter way better?
        //  Lowkey the other way would work...
        holder.imageView?.load(item){
            fallback(R.drawable.ic_placeholder_image_24)
            placeholder(R.drawable.ic_placeholder_image_24)
            error(R.drawable.ic_image_error_off_24)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}