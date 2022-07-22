package com.example.volumen.NetworkTests

import com.example.volumen.data.Link
import com.example.volumen.data.WikipediaQuery
import com.example.volumen.network.PLAINTEXT_MEDIA_TYPE
import com.example.volumen.network.WebApi
import com.example.volumen.network.logging
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Test

private const val MY_EXAMPLE_TEXT = "Regiments of Greatswords garrison the castles of the Elector Counts and form their lord's honour guard on state occasions. These grim men are equipped with huge two-handed swords called zweihanders that can cleave an armoured Knight in twain with one blow. Greatswords are also adorned with superb suits of Dwarf-forged plate armour, for these elite troops are expected to fight in the thick of the bloodiest and most dangerous combats of a battle. Upon a soldier's induction into the esteemed ranks of the Greatswords, he is required to swear an oath never to take a backwards step in the face of the enemy. Every regiment of Greatswords has its own particular punishment for those who fail in their duty. However, such instances are extremely rare, and the history of the Empire is replete with heroic tales of regiments of Greatswords that have died to a man to protect the life of their liege lord, even after the rest of their army had been butchered."
private const val MY_EXAMPLE_TEXT2 = "Regiments of Greatswords garrison the castles of the Elector Counts and form their lord's honour guard on state occasions These grim men are equipped with huge two-handed swords called zweihanders that can cleave an armoured Knight in twain with one blow. Greatswords are also adorned with superb suits of Dwarf-forged plate armour, for these elite troops are expected to fight in the thick of the bloodiest and most dangerous combats of a battle. Upon a soldier's induction into the esteemed ranks of the Greatswords, he is required to swear an oath never to take a backwards step in the face of the enemy. Every regiment of Greatswords has its own particular punishment for those who fail in their duty. However, such instances are extremely rare, and the history of the Empire is replete with heroic tales of regiments of Greatswords that have died to a man to protect the life of their liege lord, even after the rest of their army had been butchered."
// assuming default settings
private const val MY_EXAMPLE_TEXT_SUMMARY = "Regiments of Greatswords garrison the castles of the Elector Counts and form their lord's honour guard on state occasions."

private const val TAG = "NetworkTests"
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

    class WebApiServicesTest {

        @Test
        fun test_query_no_links_or_images() {
            /** Test the WikipediaApi's ability to query a page with no external links or images.
             * (Handle the null case). **/
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_no_links() = runTest {
            /** Test the WikipediaApi's ability to query a page with no links. (More null case). **/
            val fixedPageID = 865302
            val idMessage = "Note that the page ID expected is $fixedPageID"
            val expectedPageTitle = "GMC Sierra"
            val expectedPageLinks = listOf<Link>()
            val expectedPageImages = listOf("GMC_Sierra_Denali_P4250788.jpg", "GMC_Sierra_GMT800_2500HD_Techni-Air_2000_Extended_Cab.jpg")
            val testQuery = WebApi.wikipediaApiService.queryPage(expectedPageTitle)

            assertEquals(idMessage, testQuery.parsed.title, expectedPageTitle)
            assertEquals(idMessage, testQuery.parsed.relatedPages, expectedPageLinks)
            assertEquals(idMessage, testQuery.parsed.imageNames, expectedPageImages)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_basic_page() = runTest {
            /** Test the WikipediaApi's ability to query a page with both links and images. **/
            val fixedPageID = 71207007
            val idMessage = "Note that the page ID expected is $fixedPageID"
            val expectedPageTitle = "Gelmi"
            val expectedPageLinks = listOf<Link>(Link("Talk:Gelmi"), Link("Given name"), Link("Italian surname"), Link("Ludovico Gelmi"), Link("Roy Gelmi"), Link("Surname"), Link("Wikipedia:Manual of Style/Linking"))
            val expectedPageImages = listOf("WPanthroponymy.svg")
            val testQuery = WebApi.wikipediaApiService.queryPage(expectedPageTitle)

            assertEquals(idMessage, testQuery.parsed.title, expectedPageTitle)
            assertEquals(idMessage, testQuery.parsed.relatedPages, expectedPageLinks)
            assertEquals(idMessage, testQuery.parsed.imageNames, expectedPageImages)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_no_images() = runTest {
            /** Test the WikipediaApi's ability to query a page with no images. **/
            val fixedPageID = 1485297
            val idMessage = "Note that the page ID expected is $fixedPageID"
            val expectedPageTitle = "Free information"
            val expectedPageLinks = listOf<Link>(Link("Free content"), Link("Information wants to be free"))
            val expectedPageImages = listOf<String>()
            val testQuery = WebApi.wikipediaApiService.queryPage(expectedPageTitle)

            assertEquals(idMessage, testQuery.parsed.title, expectedPageTitle)
            assertEquals(idMessage, testQuery.parsed.relatedPages, expectedPageLinks)
            assertEquals(idMessage, testQuery.parsed.imageNames, expectedPageImages)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_summarize_basic_example() = runTest {
            /** Test the MeaningCloudApiService's ability to retrieve a summary from some basic text.
             * Note that summarizeAPI may give different summaries with different versions,
             * but we test with 1.0 here.
             *
             * In principle, if the function can retrieve summaries for version 1.0, it should work
             * with different versions too, assuming all that changes is how they're generated in
             * the background. */

            //TODO: For some reason, the Moshi thing quits after one sentence.
            val exampleText = MY_EXAMPLE_TEXT
            val actualSummary = WebApi.meaningCloudApiService.getSummarizedText(txt =
                exampleText.toRequestBody(PLAINTEXT_MEDIA_TYPE)).summary
            val expectedSummary = MY_EXAMPLE_TEXT_SUMMARY
            assertEquals(actualSummary, MY_EXAMPLE_TEXT_SUMMARY)
        }
    }

    class ArticleRepositoryTest {

        @Test
        fun test_article_getting_basic_example() {
             /** Test the repository's API to get an Article object from a test WikipediaQuery,
              * and some supplied example text to be summarized. See above note on summary testing.
              */
            val exampleQuery = WikipediaQuery("Example", listOf("Image1", "Image2"), listOf(Link("Link1"), Link("Link2")))
            val exampleText = MY_EXAMPLE_TEXT


        }

        @Test
        fun test_article_no_links_or_images() {
            /** Test the repository's ability to make an Article object from a page with no images or links. */
        }
    }



}