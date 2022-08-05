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
import it.skrape.selects.and
import it.skrape.selects.eachSrc
import it.skrape.selects.html5.div
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
        var imageLinkList = listOf<String>()
        var articleText = ""

        // Construct article text from heading: contents mapping from kwikipedia. Also summarize.
        // The headings are weird and tricky to format so leave them out.

        //TODO: Consider integrating heading stuff, although imo it looks weird.
        for ((heading, content) in com.neelkamath.kwikipedia.getPage(title)){
            // The see also section's contents look really ugly and aren't useful.
            if (heading != "See also") {
                articleText = articleText.plus(" ").plus(content)
            }
        }
        articleText = articleText.trim()

        val summarizedTextQuery = WebApi.meaningCloudApiService.getSummarizedText(txt =
            articleText.toRequestBody(PLAINTEXT_MEDIA_TYPE))

        // Clean the summarized text so that after every period there is a space.

        // Add underscores to the wikipedia page title so its properly formatted for the image URLs.
        // (not strictly necessary for article URL due to redirects but still helpful).
        val formattedTitle = title.replace(' ', '_')

        // Construct a wikipedia link. Use the standard schema.
        val wikipediaLink = WIKIPEDIA_PAGE_URL_PREFIX + formattedTitle

        // Construct the list of image links by webscraping.
        imageLinkList = webScrapeActualImageUrls(wikipediaLink)

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

    private fun webScrapeActualImageUrls(wikipediaPageUrl: String) : List<String> {
        /** Returns the actual image URLs on Wikimedia commons pertaining to a Wikipedia page link.
         *
         * Unfortunately the image link parameter doesn't really help and we had to do this instead.
         *
         * The initial image URL we construct according to the template actually leads us
         * to a MediaViewer page on Wikipedia. It does not actually navigate us to the image, and
         * so, we can't actually load the image with coil, hence this becomes necessary to actually
         * get usable URLs to load with.
         *
         * Uses skrape{it}, a webscraping library, to parse the site as an HTML document.
         */

        // The html class name of the <img> element shown at the MediaViewer page.
        var actualLinks : MutableList<String> = mutableListOf()

        // Use skrape to parse the site as an HTML document.
        Log.d(TAG, "URL of page to parse: $wikipediaPageUrl")
        skrape(HttpFetcher) {
            // HTML Request configured here.
            request {
                url = wikipediaPageUrl
                timeout = 180000
            }
            response {
                // In this scope everything from the response is made available. We want the document.
                htmlDocument {

                    // Okay. For some reason finding the main image using skrape is hard.
                    // But the thumbnail image can be selected easily enough so I'll do that as a
                    // workaround. I wonder why it breaks tho? Meh.
                    actualLinks = img {
                        withClass = "thumbimage"
                        findAll {
                            eachSrc
                        }
                    } as MutableList<String>
                }
            }
        }
        // These URLs get the https: part clipped. Add them back in!
        for (i in 0 until (actualLinks.size)) {
            actualLinks[i] = "https:" + actualLinks[i]
        }
        Log.d(TAG, "webScrapeActualImageUrl: $actualLinks")
        return actualLinks
    }
}