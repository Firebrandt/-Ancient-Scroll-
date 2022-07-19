package com.example.volumen.data

import com.squareup.moshi.Json

data class Link (
    /** A data class to model a 'link' JSON object received from a Wikipedia API Internal Links
     * query. Primarily for usage with the main class. This is to let us parse while trimming a few
     * extraneous attributes and just get the linked-to title.
     *
     * Instance Attributes:
     * - linkedTitle: A string representing the linked-to-title.
     */
    @Json(name = "*") val linkedTitle: String
)

data class WikipediaQuery (
    /** A data class that receives (elements of) the JSON result of a Wikipedia API query
     * (decoded into Kotlin types by Moshi).
     *
     * Remember that by default, property name = decoded JSON key name.
     *
     * Instance attributes:
     * - title: Representing the title of the queried page/article.
     * - imagesNames: A list of Strings representing the names of images on the page. (Not URLs!)
     * - relatedPages: A list of Links storing, essentially, Wikipedia page titles linked from this one.
     */
    val title: String,
    @Json(name = "images") val imageNames : List<String>,
    val relatedPages: List<Link>
)