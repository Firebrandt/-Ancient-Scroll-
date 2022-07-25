package com.example.volumen.uicontrollers

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.R
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ListAdapter
import com.neelkamath.kwikipedia.getPage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    //TODO: Test the recycler view and see if it works. Not sure why the integration isn't working, unsure if it's data loading or recycler view display.
    // TODO: Get the recycler view load image thing working.
    private val viewModel: ItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the recycler view.
        val adapter = ItemListAdapter(::updateViewModel)
        binding.itemList.adapter = adapter
        binding.itemList.layoutManager = LinearLayoutManager(this)

        // Try to set up the dataset...
//        GlobalScope.launch {
//            viewModel.getDataSet()
//        }
        val dummyArticle = Article(0, listOf(), "Tiny text!", "Dummy!", "No URL!")
        val dummyList = mutableListOf<Article>()
        repeat(5) {
            dummyList.add(dummyArticle)
        }
        adapter.submitList(dummyList)
    }

    fun updateViewModel(clickedArticle: Article) {

    }
}