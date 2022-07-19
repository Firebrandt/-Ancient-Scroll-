package com.example.volumen.NetworkTests

import org.junit.Test

class NetworkTests {
    /** A class storing tests for the app's functionality to fetch data from the network.
     *
     * NOTE: For Wikipedia,
     * Unit tests are prone to break in the case where a page is revised. A hardcoded
     * fixedPageID is provided to provide some indication of this - a DEBUG log will be raised if
     * it is the case.
     *
     * Without making the original function take in a revisionId to request a fixed revision,
     * solely for testing purposes, (which overrides page title and is impossible to use!), this is
     * difficult to fix.
     */

    //TODO: Design the tests I want to write. Tired so won't implement today.
    class WebApiServicesTest {

        @Test
        fun test_query_no_links_or_images() {
            /** Test the WikipediaApi's ability to query a page with no external links or images.
             * (Handle the null case). **/
            val fixedPageID = 865302
            val pageTitle = "GMC Sierra"
        }

        @Test
        fun test_query_basic_page() {
            /** Test WikipediaApi's ability to query a basic page with some images and links. **/
        }

    }

    class ArticleRepositoryTest {

        @Test
        fun test_article_getting_basic_example() {
             /** Test the repository's API to get an Article object from a basic page w/ some images and links */

        }

        @Test
        fun test_article_no_links_or_images() {
            /** Test the repository's ability to make an Article object from a page with no images or links. */
        }
    }



}