package com.example.volumen.viewModels



import android.util.Log
import androidx.lifecycle.*
import com.example.volumen.data.Article
import com.example.volumen.data.ArticleDao
import com.example.volumen.data.ImageUrlsDao
import com.example.volumen.data.JunctionDao
import com.example.volumen.repository.ArticleRepository

//TODO: This is prone to break if the page title is edited at any time.
// Later on put in some logic for this I suppose. Use pageID. Maybe.

private const val OUTLINE_PAGE_TITLE = "Outline of ancient Rome"
private const val TAG = "ItemViewModel"

private val DUMMY_ARTICLE = Article(title = "", summarized = "Hold on while we do some loading.", originalURL = "")

class ItemViewModel(val articleDao: ArticleDao, imageUrlsDao: ImageUrlsDao, junctionDao: JunctionDao) : ViewModel() {
    /** A view model housing important data and data processing for MainActivity
     * and ItemDetailFragment, relating to the display of articles.
     */

    private val repository = ArticleRepository(articleDao, imageUrlsDao, junctionDao)
    private val _currentArticle: MutableLiveData<Article> = MutableLiveData(DUMMY_ARTICLE)
    val currentArticle: LiveData<Article> get() = _currentArticle
    private var _dataset = mutableListOf<Article>()

    suspend fun getCachedData() : List<Article> {
        /** Get the list of cached articles for the app. Generally quicker than loading things in
         * from the outline page via getOnlineData(). Just interfaces with the repository to do this.
         */
        val cachedArticles = repository.getCachedArticles()
        Log.d(TAG, "getDataSet: $cachedArticles")
        _dataset.addAll(cachedArticles)
        return cachedArticles
    }

    suspend fun getOnlineData() : List<Article> {
        /** Get the full list of non-cached articles for the app from an outline page.
         * Mutate _dataset to store and expose it.
         *
         * Interface with the repository to do this. For now, we have fixed updates.
         * Live updates can be added later (although they probably won't be necessary).
         */
        // Definitely test the recycler view to see if something actually happens... and it displays stuff...
        val newValue = repository.getRelatedPages(OUTLINE_PAGE_TITLE)
        Log.d(TAG, "getDataSet: $newValue")
        _dataset.addAll(newValue)
        return newValue
    }

    fun updateCurrentArticle(newArticle: Article) {
        _currentArticle.value = newArticle
    }

    fun clearCache(){
        /** Clears the entire local database of everything. Careful with this! **/
        repository.clearCache()
    }
}

class ItemViewModelFactory(private val articleDao: ArticleDao, private val imageUrlsDao: ImageUrlsDao, private val junctionDao: JunctionDao) : ViewModelProvider.Factory {
    /** A custom factory to initialise an ItemViewModel with an articleDao, in accordance with
     * best practises.
     */
    // TODO: Read up on type parameter syntax.
    // Basically, check if we can make a view model of that type. Return it if so. Otherwise
    // throw an exception.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(articleDao, imageUrlsDao, junctionDao) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class for this factory!")
        }
    }
}