package com.example.volumen.viewModels



import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.volumen.data.Article
import com.example.volumen.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

//TODO: This is prone to break if the page title is edited at any time.
// Later on put in some logic for this I suppose. Use pageID. Maybe.

private const val OUTLINE_PAGE_TITLE = "Outline of ancient Rome"
private const val TAG = "ItemViewModel"

private val DUMMY_ARTICLE = Article(0, listOf(), "", "Hold on while we do some loading.", "")

class ItemViewModel : ViewModel() {
    /** A view model housing important data and data processing for MainActivity
     * and ItemDetailFragment, relating to the display of articles.
     */

    private val repository = ArticleRepository()
    private val _currentArticle: MutableLiveData<Article> = MutableLiveData(DUMMY_ARTICLE)
    val currentArticle: LiveData<Article> get() = _currentArticle
    lateinit var dataset: List<Article>


    suspend fun getDataSet() : List<Article> {
        /** Get the full list of articles for the app from an outline page. Mutate _dataset
         * to store it.
         *
         * Interface with the repository to do this. For now, we have fixed updates.
         * Live updates can be added later (although they probably won't be necessary).
         */
        // Definitely test the recycler view to see if something actually happens... and it displays stuff...
        val newValue = repository.getRelatedPages(OUTLINE_PAGE_TITLE)
        Log.d(TAG, "getDataSet: $newValue")
        dataset = newValue
        return newValue
    }

    fun updateCurrentArticle(newArticle: Article) {
        _currentArticle.value = newArticle
    }
}