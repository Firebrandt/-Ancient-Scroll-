package com.example.volumen.uicontrollers

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.R
import com.example.volumen.adapters.DetailsItemListAdapter
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.application.MyApplication
import com.example.volumen.data.Article
import com.example.volumen.databinding.FragmentItemDetailBinding
import com.example.volumen.viewModels.ItemViewModel
import com.example.volumen.viewModels.ItemViewModelFactory
import com.google.android.material.snackbar.Snackbar

private const val TAG = "ItemDetailFragment"
class ItemDetailFragment : Fragment() {

    // UGH! Accidentally made a fragment-level view model instead of an activity level one,
    // de-syncing the two. Ew! Ew! Ew! Ew! Ew! Ew!!!
    private val viewModel: ItemViewModel by activityViewModels(){
        val appDatabase = (activity?.application as MyApplication).appDatabase
        ItemViewModelFactory(appDatabase.getArticleDao(),
            appDatabase.getImageUrlsDao(),
            appDatabase.getJunctionsDao())
    }

    private lateinit var binding: FragmentItemDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Set up the data binding once we've initialized stuff.
        binding = FragmentItemDetailBinding.inflate(layoutInflater, container, false)
        binding.myViewModel = viewModel
        // Include a reference to the fragment purely to data bind on click listeners.
        binding.itemDetailFragment = this
        // Remember we need to pass the binding a life cycle owner for it to auto-update properly.
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up the recycler view as well.
        Log.i(TAG, "We may attempt loading the detailsImageList now! While we're doing " +
                "this, note the current article is ${viewModel.currentArticle.value}")

        viewModel.currentArticle.value?.imageList?.let {
            val adapter = DetailsItemListAdapter(it)
            binding.detailsImageList.adapter = adapter

            val layoutManager = LinearLayoutManager(this.requireContext())
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            binding.detailsImageList.layoutManager = layoutManager
            Log.i(TAG, "detailsImageList loading complete! Used $it")
        }

        // Observe current article to update the recycler view whenever we get a new list.
        viewModel.currentArticle.observe(viewLifecycleOwner) {
            Log.i(
                TAG,
                "onCreateView: We're actually observing the article change! ${it} is the one that appears to have updated."
            )

            val adapter = DetailsItemListAdapter(it.imageList ?: listOf())
            binding.detailsImageList.adapter = adapter
            binding.manualTitle.text = it.title
            binding.executePendingBindings()

            Log.i(
                TAG,
                "onCreateView: We tried to set up the binding. We have set it to ${binding.manualTitle.text} manually"
            )
        }

        // THIS BROKE IT BEFORE!!!@ ASDJAWDAWD
        //  BUT SETTING IT TO BINDING WORKED! D;;;;;;;;;
        //  Probably BECAUSE OUR CHANGES ARE TO THE BINDING AND NOT THE RAW LAYOUT FILE! AHHH!
        return binding.root
//        DO NOT USE THIS LINE! return inflater.inflate(R.layout.fragment_item_detail, container, false)
    }

    fun gotoArticle() {
        /** A function that sets an implicit intent to open the original wikipedia article
         * in a browser of some kind (handled by the system), and launches it.
         *
         * Meant to be used as an on Click listener for a navigate button.
         */
        val articleIntent : Intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(viewModel.currentArticle.value?.originalURL))

        try {
            startActivity(articleIntent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(binding.root,
                "We couldn't find an app that could launch the wikipedia link.",
                Snackbar.LENGTH_SHORT).show()
        }
    }


}