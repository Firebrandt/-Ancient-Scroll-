package com.example.volumen.repository

import android.util.Log
import com.example.volumen.data.Article
import com.example.volumen.network.PLAINTEXT_MEDIA_TYPE
import com.example.volumen.network.WebApi
import it.skrape.core.htmlDocument
import it.skrape.fetcher.AsyncFetcher
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.img
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

        val summarizedTextQuery = WebApi.meaningCloudApiService.getSummarizedText(txt =
            articleText.toRequestBody(PLAINTEXT_MEDIA_TYPE))

        // Add underscores to the wikipedia page title so its properly formatted for the image URLs.
        // (not strictly necessary for article URL due to redirects but still helpful).
        val formattedTitle = title.replace(' ', '_')

        // Construct the list of image links. Starting from predictable MediaViewer links, and
        // web-scraping the actual source of the image.
        for (name in wikiQuery.parsed.imageNames) {
            val fakeUrl = "https://en.wikipedia.org/wiki/$formattedTitle#/media/File:$name"
            val imgUrl = webScrapeActualImageUrl(fakeUrl)
            imgUrl?.let {
                imageLinkList.add(it)
            }
        }

        // Construct a wikipedia link. Use the standard schema.
        val wikipediaLink = WIKIPEDIA_PAGE_URL_PREFIX + formattedTitle

        // Make the article and return it. Don't use the URL formatted title for display though.
        val returnedArticle = Article(imageList = imageLinkList, summarized = summarizedTextQuery.summary,
            title = title, originalURL = wikipediaLink)

        Log.d(TAG, "Returned an article: $returnedArticle")
        return returnedArticle
    }

    suspend fun getRelatedPages(title: String) : List<Article> {
        /** Generate a list of Articles for wikipedia pages that are linked on this one.
         * This is primarily useful for sourcing a large number of wikipedia Articles from a central
         * outline/glossary type page.
         */

        val relatedPages = WebApi.wikipediaApiService.queryPage(title).parsed.relatedPages

        val articleList = mutableListOf<Article>()
        for (link in relatedPages) {
            // Skip empty articles. Note that this function gives us a map, not text.
            val articleTextMap = com.neelkamath.kwikipedia.getPage(link.linkedTitle)
            if (articleList.size == 1){
                break
            }
            if (articleTextMap.isNotEmpty()) {
                articleList.add(getArticle(link.linkedTitle))
            }
        }

        Log.d(TAG, "Article List: $articleList")
        return articleList
    }

    private fun webScrapeActualImageUrl(fakeWikipediaImageUrl: String) : String? {
        /** Returns the actual image URL on Wikimedia commons pertaining to a Wikipedia 'image link'.
         *
         * The initial image URL we construct according to the template actually leads us
         * to a MediaViewer page on Wikipedia. It does not actually navigate us to the image, and
         * so, we can't actually load the image with coil, hence this becomes necessary to actually
         * load the bloody thing.
         *
         * Uses skrape{it}, a webscraping library, to parse the site as an HTML document.
         */

        // The html class name of the <img> element shown at the MediaViewer page.
        val MEDIA_VIEWER_OPENED_IMAGE_CLASS_NAME = "jpg mw-mmv-dialog-is-open"

        var actualLinks : List<String>? = null

        //TODO: It's saying it cannot find the element. Do try to debug this. Maybe nest harder.
        // Maybe selecting with the CSS will work. I mean it does say CSS selector, right?

        // Use skrape to parse the site as an HTML document.
        Log.d(TAG, "webScrapeActualImageUrl: $fakeWikipediaImageUrl")
        skrape(HttpFetcher) {
            // HTML Request configured here.
            request {
                url = fakeWikipediaImageUrl
                timeout = 180000
            }
            response {
                // In this scope everything from the response is made available. We want the document.
                htmlDocument {
                    // Parsed document is available in this scope.
                    actualLinks = findFirst {
                        // Find the first image with appropriate class, and basically, take its first source.
                        img {
                            withClass = MEDIA_VIEWER_OPENED_IMAGE_CLASS_NAME
                            findFirst {
                                eachSrc
                            }
                        }
                    }
                }
            }
        }
        return actualLinks?.get(0)
    }
}