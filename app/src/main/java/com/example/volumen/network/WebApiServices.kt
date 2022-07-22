package com.example.volumen.network

import com.example.volumen.data.Parsed
import com.example.volumen.data.SummarizeQuery
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val SUMMARIZE_VERSION = "1.0"
private const val ENG_WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/w/"
private const val MEANING_CLOUD_BASE_URL = "https://api.meaningcloud.com/"
private const val MEANING_CLOUD_SUMMARIZE_ENDPOINT = "summarization-$SUMMARIZE_VERSION"
private const val WIKI_PARSE_ENDPOINT = "api.php?action=parse&format=json&prop=links|images"
private const val MY_KEY = "c66278a6ad1b80bf1656234b1ffb00ea"

// For some reason when setting these, we just have to copy paste constants???
// Anyway, lets us specify the content-type/media type we want a bit of RequestBody to be.
val PLAINTEXT_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

// Set up some logging for Retrofit so I can see raw response and debug.
val logging = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.BODY)

val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

// Define a retrofit object and use Moshi to convert JSON to Kotlin data class instances.
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofitWikipedia = Retrofit.Builder()
    .baseUrl(ENG_WIKIPEDIA_BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

val retrofitMeaningCloud = Retrofit.Builder()
    .baseUrl(MEANING_CLOUD_BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()




interface WikipediaApiService {
    /** An interfacing defining how Retrofit will interact with the Wikipedia API.
     * Recall that GET requests specify the endpoint of the request.
     */

    @GET(WIKI_PARSE_ENDPOINT)
    suspend fun queryPage(@Query("page") title: String) : Parsed
    /** Return a WikipediaQuery corresponding to a particular page. */
}

interface MeaningCloudApiService {
    /** An interface defining how Retrofit will interact with the Meaningcloud API. **/

    // TODO: Figure out WTF this part stuff is. What am I doing here, just sending key/value pairs??
    //  How does this part multipart shit work? WTF is retrofit doing? Do I need headers?
    //  Are the parameters mentioned there parts I need to supply?
    // The request body direct initialisation is needed because retrofit will DOUBLE QUOTE string parameters!!! BREAKING THE REQUEST!! aghaAGAHGAHAHAAAA!!!
    @Multipart
    @POST(MEANING_CLOUD_SUMMARIZE_ENDPOINT)
    suspend fun getSummarizedText(@Part("key") key: RequestBody = MY_KEY.toRequestBody(PLAINTEXT_MEDIA_TYPE),
                                  @Part("txt") txt: RequestBody,
                                  @Part("lang") lang: RequestBody = "en".toRequestBody(PLAINTEXT_MEDIA_TYPE),
                                  @Part("limit") limit: Int = 100) : SummarizeQuery
    /** Send a POST request to the MeaningCloud summarize endpoint to retrieve a summary of some text. */
}

object WebApi {
    /** Object used to expose the web services we define here as a singleton, since initialising it
     * is expensive and we only need one. */
    val wikipediaApiService by lazy {
        retrofitWikipedia.create(WikipediaApiService::class.java)
    }

    val meaningCloudApiService by lazy {
        retrofitMeaningCloud.create(MeaningCloudApiService::class.java)
    }
}





