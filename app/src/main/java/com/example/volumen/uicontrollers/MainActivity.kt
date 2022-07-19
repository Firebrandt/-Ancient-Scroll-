package com.example.volumen.uicontrollers

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.R
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.databinding.ActivityMainBinding
import com.neelkamath.kwikipedia.getPage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the recycler view.
        binding.itemList.adapter = ItemListAdapter()
        binding.itemList.layoutManager = LinearLayoutManager(this)
    }
}