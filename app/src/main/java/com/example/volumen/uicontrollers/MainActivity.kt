package com.example.volumen.uicontrollers

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.data.Article
import com.example.volumen.databinding.ActivityMainBinding
import com.example.volumen.viewModels.ItemViewModel
import kotlinx.coroutines.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
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

        lifecycleScope.launch(Dispatchers.IO) {
            val dataset = viewModel.getDataSet()

            withContext(Dispatchers.Main) {
                adapter.submitList(dataset)
            }
        }


    }

    fun updateViewModel(clickedArticle: Article) {

    }
}

//// Generates some dummy list items
//val dummyArticle = Article(0, listOf(), "Tiny text!", "Dummy!", "No URL!")
//val dummyList = mutableListOf<Article>()
//repeat(5) {
//    dummyList.add(dummyArticle)
//}