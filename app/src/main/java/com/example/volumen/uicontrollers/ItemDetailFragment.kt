package com.example.volumen.uicontrollers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volumen.R
import com.example.volumen.adapters.DetailsItemListAdapter
import com.example.volumen.adapters.ItemListAdapter
import com.example.volumen.data.Article
import com.example.volumen.databinding.FragmentItemDetailBinding
import com.example.volumen.viewModels.ItemViewModel

private const val TAG = "ItemDetailFragment"
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
        // TBH the recycler view thing makes the most sense. This should only set up once!

        // Set up the data binding once we've initialized stuff.
        binding = FragmentItemDetailBinding.inflate(layoutInflater, container, false)
        binding.myViewModel = viewModel

        //TODO: Pretty sure I need to pass a life cycle owner to the binding for live data updates to work?
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up the recycler view as well.
        Log.i(TAG, "We may attempt loading the detailsImageList now! While we're doing " +
                "this, note the current article is ${viewModel.currentArticle.value}")

        viewModel.currentArticle.value?.imageList?.let {
            val adapter = DetailsItemListAdapter(it)
            // binding.detailsImageList.adapter = adapter

            val layoutManager = LinearLayoutManager(this.requireContext())
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            // binding.detailsImageList.layoutManager = layoutManager
            Log.i(TAG, "detailsImageList loading complete! Used $it")
        }

        // Observe current article to update the recycler view whenever we get a new list.
        viewModel.currentArticle.observe(viewLifecycleOwner) {
            Log.i(TAG, "onCreateView: We're actually observing the article change! ${it} is the one that appears to have updated.")

            val adapter = DetailsItemListAdapter(it.imageList ?: listOf())
            // binding.detailsImageList.adapter = adapter
            binding.manualTitle.text = it.title
            binding.executePendingBindings()

            Log.i(TAG, "onCreateView: We tried to set up the binding. We have set it to ${binding.manualTitle.text} manually," +
                    "and ${binding.normalTitle.text} via automatic data binding.")

            //TODO: Damn it seems to be a UI problem! Sheesh! Since manual doesn't work either.
            // Okay so it's a data binding problem. The article comes through properly and everything. But somehow the data bindings don't update, at all...
            // Hm.

            // TODO: Wait. The binding values seem to come through and everything, they just don't display!
            //  WTF!!!!!! So it probably is UI side.
            //  Okay, I have no idea what this is.

            //
        }

        // TODO: THIS BROKE IT BEFORE!!!@ ASDJAWDAWD
        //  BUT SETTING IT TO BINDING WORKED! D;;;;;;;;;
        //  Probably BECAUSE OUR CHANGES ARE TO THE BINDING AND NOT THE RAW LAYOUT FILE!
        return binding.root
//        return inflater.inflate(R.layout.fragment_item_detail, container, false)
    }

}