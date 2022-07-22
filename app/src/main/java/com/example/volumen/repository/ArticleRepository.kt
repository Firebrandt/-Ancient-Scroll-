package com.example.volumen.repository

import com.example.volumen.data.Article
import com.example.volumen.data.WikipediaQuery
import com.example.volumen.network.PLAINTEXT_MEDIA_TYPE
import com.example.volumen.network.WebApi
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.await

private const val WIKIPEDIA_PAGE_URL_PREFIX = "https://en.wikipedia.org/wiki/"
class ArticleRepository {
    /** Repository class meant to provide a clean API for several data sources, resolve conflicts,
     * and hide implementation details of the 'data layer' work like working with a database. **/

    suspend fun getArticle(title: String) : Article {
        /** Returns an Article object based on an Article title. Queries the Wikipedia API
         * in the background, hence the suspend keyword.
         */
        val wikiQuery = WebApi.wikipediaApiService.queryPage(title)
        val imageLinkList = mutableListOf<String>()
        var articleText = ""

        // Construct article text from heading: contents mapping from kwikipedia. Also summarize.
        for ((heading, content) in com.neelkamath.kwikipedia.getPage(title)){
            articleText = articleText.plus("$heading \n").plus(content)
        }

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

    suspend fun queryRelatedPages(title: String) : List<WikipediaQuery> {
        TODO()
    }
    /** Return a List<WikipediaQuery> corresponding to all pages linked to the inputted one. */
}