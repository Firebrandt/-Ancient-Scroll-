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

data class Parsed(
    /** A data class that serves to receive the JSON result of a Wikipedia API query.
     * (decoded into Kotlin types by moshi). This one just models a wrapper.
     *
     * Instance attributes:
     * - parsed: the WikipediaQuery parsed by this request.
     */
    @Json(name = "parse") val parsed: WikipediaQuery
)

data class WikipediaQuery (
    /** A data class that receives the 'parsed' the JSON result of a Wikipedia API query
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
    @Json(name = "links") val relatedPages: List<Link>,
)

data class SummarizeStatus(
    /** A data class used to model part of the MeaningCloud summarize API query. Specifically
     * the status portion.
     *
     * Instance attributes:
     * - code: The status code
     * - msg: The status message
     * - credits: The number of API credits the request used
     * - remainingCredits: the number of API credits we have left this month.
     */
    val code: String,
    val msg: String,
    val credits: Int,
    @Json(name = "remaining_credits") val remainingCredits: Int
)

data class SummarizeQuery(
    /** A data class that receives the JSON result of a MeaningCloud Summarize API query.
     * Instance attributes:
     * - summary: The summarized text.
     * - status: A few notes on the status of the request.
     */
    val status: SummarizeStatus,
    val summary: String

)