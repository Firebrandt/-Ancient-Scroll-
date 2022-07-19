package com.example.volumen.network

import com.example.volumen.data.WikipediaQuery
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val ENG_WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/w/api.php"

private const val PARSE_ENDPOINT = "?action=parse&format=json&prop=links|images"

// Define a retrofit object and use Moshi to convert JSON to Kotlin data class instances.
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl(ENG_WIKIPEDIA_BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface WikipediaApiService {
    /** An interfacing defining how Retrofit will interact with the Wikipedia API.
     * Recall that GET requests specify the endpoint of the request.
     */

    @GET("?action=parse&format=json&prop=links|images")
    suspend fun queryPage(@Query("page") title: String) : WikipediaQuery
    /** Return a WikipediaQuery corresponding to a particular page. */
}

object WikipediaApi {
    /** Object used to expose the wikipediaApiService as a singleton, since initialising it
     * is expensive and we only need one. */
    val wikipediaApiService by lazy {
        retrofit.create(WikipediaApiService::class.java)
    }
}





