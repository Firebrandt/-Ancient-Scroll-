package com.example.volumen.bindingAdapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.volumen.R

//TODO: Adding error handling would be nice here to give the user some status updates.

// TODO: Also, maybe make a listData attribute in data binding so that ostensibly all the
//  UI Info is in the file.
@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    /** A binding adapter that handles the imageUrl custom ImageView attribute by either loading
     * the image into the view, or displaying a placeholder image if the url is null.
     */
    view.load(url)
}