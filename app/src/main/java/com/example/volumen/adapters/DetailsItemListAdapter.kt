package com.example.volumen.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.qos.logback.classic.spi.CallerData.extract
import coil.ImageLoader
import coil.load
import com.example.volumen.R
import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.matchers.toBe
import it.skrape.selects.html5.img


class DetailsItemListAdapter(val imageList: List<String>) : RecyclerView.Adapter<DetailsItemListAdapter.DetailsImageViewHolder>() {
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
        // TODO: This doesn't load anything at all.
        //  Does not work with a hardcoded jpg link.
        //  ... from wikipedia. I think the issue here is that the wikipedia page doesn't actually
        //  direct me to an image even though the URL suggests it does and it seems to. It directs me to the weird wikipedia preview page,
        //  and the actual image is somewhere else. Ugh.
        //  Unfortunately since I only have image title and maybe date to work with, I don't think I can easily get the URL.
        //  The thing I can think of right now is to do a tiny bit of webscraping to get the URL from clicking and then use that.
        //  ... sigh...
        holder.imageView?.load(item)

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

}