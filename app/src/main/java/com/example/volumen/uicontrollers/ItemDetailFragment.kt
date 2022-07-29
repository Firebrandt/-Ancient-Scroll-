package com.example.volumen.uicontrollers

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.R
import com.example.volumen.adapters.DetailsItemListAdapter
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.databinding.FragmentItemDetailBinding
import com.example.volumen.viewModels.ItemViewModel

class ItemDetailFragment : Fragment() {

    private val viewModel: ItemViewModel by activityViewModels()
    private lateinit var binding: FragmentItemDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //TODO: A lot of work needs to be done on figuring out why shit doesn't appear in the details view.
        // Do some debugging and focus on that. I think a previous hypothesis was that we were staying
        // On an old current article and not observing it properly (staying on the fake article).
        // but right now NOTHING shows and it is LiveData. Hmph. Maybe it's a null thing too.
        // Or its a recycler view thing, actually.

        // Set up the data binding once we've initialized stuff.
        binding = FragmentItemDetailBinding.inflate(layoutInflater)
        binding.viewModel = this.viewModel

        // Set up the recycler view as well.
        viewModel.currentArticle.value?.imageList?.let {
            val adapter = DetailsItemListAdapter(it)
            binding.detailsImageList.adapter = adapter

            val layoutManager = LinearLayoutManager(this.requireContext())
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            binding.detailsImageList.layoutManager = layoutManager
        }
        return inflater.inflate(R.layout.fragment_item_detail, container, false)
    }

}