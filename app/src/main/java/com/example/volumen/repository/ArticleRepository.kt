package com.example.volumen.repository

import com.example.volumen.data.Article
import com.example.volumen.data.WikipediaQuery
import com.example.volumen.network.WikipediaApi

private const val WIKIPEDIA_PAGE_URL_PREFIX = "https://en.wikipedia.org/wiki/"
class ArticleRepository {
    /** Repository class meant to provide a clean API for several data sources, resolve conflicts,
     * and hide implementation details of the 'data layer' work like working with a database. **/

    suspend fun getArticle(title: String) : Article {
        /** Returns an Article object based on an Article title. Queries the Wikipedia API
         * in the background, hence the suspend keyword.
         */
        val wikiQuery = WikipediaApi.wikipediaApiService.queryPage(title)
        val imageLinkList = mutableListOf<String>()
        var articleText = ""

        // Construct article text from heading: contents mapping
        for ((heading, content) in com.neelkamath.kwikipedia.getPage(title)){
            articleText = articleText.plus("$heading \n").plus(content)
        }

        // Format an image name to its EN wikipedia URL. There's a standard scheme.
        for (name in wikiQuery.imageNames) {
            imageLinkList.add("https://en.wikipedia.org/wiki/$title#/media/File:$name")
        }

        // Construct a wikipedia link. Use the standard schema.
        val wikipediaLink = WIKIPEDIA_PAGE_URL_PREFIX + title

        // TODO: Make the summarized text actually work.
        return Article(imageList = imageLinkList, summarized = articleText, title = title, originalURL = wikipediaLink)
    }

    suspend fun queryRelatedPages(title: String) : List<WikipediaQuery>
    /** Return a List<WikipediaQuery> corresponding to all pages linked to the inputted one. */
}