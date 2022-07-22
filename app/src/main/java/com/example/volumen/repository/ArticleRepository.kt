package com.example.volumen.repository

import android.util.Log
import com.example.volumen.data.Article
import com.example.volumen.data.WikipediaQuery
import com.example.volumen.network.PLAINTEXT_MEDIA_TYPE
import com.example.volumen.network.WebApi
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "Repository"
private const val WIKIPEDIA_PAGE_URL_PREFIX = "https://en.wikipedia.org/wiki/"
class ArticleRepository {
    /** Repository class meant to provide a clean API for several data sources, resolve conflicts,
     * and hide implementation details of the 'data layer' work like working with a database. **/

    suspend fun getArticle(title: String) : Article {
        /** Returns an Article object based on an Article title.
         * Queries the Wikipedia API in the background, hence the suspend keyword.
         */
        val wikiQuery = WebApi.wikipediaApiService.queryPage(title)
        val imageLinkList = mutableListOf<String>()
        var articleText = ""

        // Construct article text from heading: contents mapping from kwikipedia. Also summarize.
        for ((heading, content) in com.neelkamath.kwikipedia.getPage(title)){
            articleText = articleText.plus("$heading \n").plus(content)
        }

        Log.d(TAG, "The article text is: $articleText")
        val summarizedTextQuery = WebApi.meaningCloudApiService.getSummarizedText(txt =
            articleText.toRequestBody(PLAINTEXT_MEDIA_TYPE))

        // Format an image name to its EN wikipedia URL. There's a standard scheme.
        for (name in wikiQuery.parsed.imageNames) {
            imageLinkList.add("https://en.wikipedia.org/wiki/$title#/media/File:$name")
        }

        // Construct a wikipedia link. Use the standard schema.
        val wikipediaLink = WIKIPEDIA_PAGE_URL_PREFIX + title

        // Make the article and return it.
        return Article(imageList = imageLinkList, summarized = summarizedTextQuery.summary,
            title = title, originalURL = wikipediaLink)
    }

    suspend fun getRelatedPages(title: String) : List<Article> {
        /** Generate a list of Articles for wikipedia pages that are linked on this one.
         * This is primarily useful for sourcing a large number of wikipedia Articles from a central
         * outline/glossary type page.
         */

        //TODO: Get this working. A possible bug source is that the text is empty for some pages.
        // (The API doesn't recognize entered text).
        // This messes up the POST request. Investigate more tmr.

        val relatedPages = WebApi.wikipediaApiService.queryPage(title).parsed.relatedPages

        val articleList = mutableListOf<Article>()
        for (link in relatedPages) {
            // Skip empty articles.

            val articleText = com.neelkamath.kwikipedia.getPage(link.linkedTitle)
            if (articleText.toString() != "") {
                articleList.add(getArticle(link.linkedTitle))
            }
        }

        return articleList
    }
    /** Return a List<WikipediaQuery> corresponding to all pages linked to the inputted one. */
}