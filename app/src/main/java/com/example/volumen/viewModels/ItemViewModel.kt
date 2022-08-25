@file:Suppress("unused")

package com.example.volumen.viewModels



import android.util.Log
import androidx.lifecycle.*
import com.example.volumen.data.Article
import com.example.volumen.data.ArticleDao
import com.example.volumen.data.ImageUrlsDao
import com.example.volumen.data.JunctionDao
import com.example.volumen.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "ItemViewModel"

private val DUMMY_ARTICLE = Article(title = "", summarized = "Hold on while we do some loading.", originalURL = "")

class ItemViewModel(articleDao: ArticleDao, imageUrlsDao: ImageUrlsDao, junctionDao: JunctionDao) : ViewModel() {
    /** A view model housing important data and data processing for MainActivity
     * and ItemDetailFragment, relating to the display of articles.
     */

    private val repository = ArticleRepository(articleDao, imageUrlsDao, junctionDao)
    private val _currentArticle: MutableLiveData<Article> = MutableLiveData(DUMMY_ARTICLE)
    val currentArticle: LiveData<Article> get() = _currentArticle

    fun getCachedData() : List<Article> {
        /** Get the list of cached articles for the app. Generally quicker than loading things in
         * from the outline page via the BackgroundWorker.
         * Just interfaces with the repository to do this.
         */
        val cachedArticles = repository.getCachedArticles()
        Log.d(TAG, "getDataSet: $cachedArticles")
        return cachedArticles
    }

    fun updateCurrentArticle(newArticle: Article) {
        _currentArticle.value = newArticle
    }

    fun clearCache(){
        /** Clears the entire local database of everything. Careful with this! **/
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCache()
        }
    }
}

class ItemViewModelFactory(private val articleDao: ArticleDao, private val imageUrlsDao: ImageUrlsDao, private val junctionDao: JunctionDao) : ViewModelProvider.Factory {
    /** A custom factory to initialise an ItemViewModel with an articleDao, in accordance with
     * best practises. Largely following boilerplate code.
     */
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