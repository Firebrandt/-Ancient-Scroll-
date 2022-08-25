@file:Suppress("unused")

package com.example.volumen.NetworkTests

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.example.volumen.application.MyApplication
import com.example.volumen.data.*
import com.example.volumen.network.PLAINTEXT_MEDIA_TYPE
import com.example.volumen.network.WebApi
import com.example.volumen.repository.ArticleRepository
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

private const val MY_EXAMPLE_TEXT = "Regiments of Greatswords garrison the castles of the Elector Counts and form their lord's honour guard on state occasions. These grim men are equipped with huge two-handed swords called zweihanders that can cleave an armoured Knight in twain with one blow. Greatswords are also adorned with superb suits of Dwarf-forged plate armour, for these elite troops are expected to fight in the thick of the bloodiest and most dangerous combats of a battle. Upon a soldier's induction into the esteemed ranks of the Greatswords, he is required to swear an oath never to take a backwards step in the face of the enemy. Every regiment of Greatswords has its own particular punishment for those who fail in their duty. However, such instances are extremely rare, and the history of the Empire is replete with heroic tales of regiments of Greatswords that have died to a man to protect the life of their liege lord, even after the rest of their army had been butchered."

// assuming settings in the test.
private const val MY_EXAMPLE_TEXT_SUMMARY = "Regiments of Greatswords garrison the castles of the Elector Counts and form their lord's honour guard on state occasions. Every regiment of Greatswords has its own particular punishment for those who fail in their duty."

class NetworkTests {
    /** A class storing tests for the app's functionality to fetch data from the network.
     *
     * NOTE: For Wikipedia,
     * Unit tests are prone to break in the case where a page is revised. A hardcoded
     * revisionid is used to 'freeze' the page revision instead, so that testing is possible, altho
     * this isn't perfect (since we don't test the title one).
     */

    class WebApiServicesTest {

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_no_links() = runTest {
            /** Test the WikipediaApi's ability to query a page with no links. (More null case). **/
            val fixedRevisionId = 1099178947
            val expectedPageTitle = "GMC Sierra"
            val expectedPageLinks = listOf<Link>()
            val expectedPageImages = listOf("GMC_Sierra_Denali_P4250788.jpg", "GMC_Sierra_GMT800_2500HD_Techni-Air_2000_Extended_Cab.jpg")
            val testQuery = WebApi.wikipediaApiService.queryPage(revisionId = 1099178947)

            assertEquals(expectedPageTitle, testQuery.parsed.title)
            assertEquals(expectedPageLinks, testQuery.parsed.relatedPages)
            assertEquals(expectedPageImages, testQuery.parsed.imageNames)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_basic_page() = runTest {
            /** Test the WikipediaApi's ability to query a page with both links and images. **/
            val fixedRevisionID = 1097532676
            val expectedPageTitle = "Gelmi"
            val expectedPageLinks = listOf(Link("Talk:Gelmi"), Link("Given name"), Link("Italian surname"), Link("Ludovico Gelmi"), Link("Roy Gelmi"), Link("Surname"), Link("Wikipedia:Manual of Style/Linking"))
            val expectedPageImages = listOf("WPanthroponymy.svg")
            val testQuery = WebApi.wikipediaApiService.queryPage(revisionId = fixedRevisionID)

            assertEquals(expectedPageTitle, testQuery.parsed.title)
            assertEquals(expectedPageLinks, testQuery.parsed.relatedPages)
            assertEquals(expectedPageImages, testQuery.parsed.imageNames)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_query_no_images() = runTest {
            /** Test the WikipediaApi's ability to query a page with no images. **/
            val fixedRevisionID = 1099563720
            val expectedPageTitle = "Free information"
            val expectedPageLinks = listOf(Link("Free content"), Link("Information wants to be free"))
            val expectedPageImages = listOf<String>()
            val testQuery = WebApi.wikipediaApiService.queryPage(revisionId = fixedRevisionID)

            assertEquals(expectedPageTitle, testQuery.parsed.title)
            assertEquals(expectedPageLinks, testQuery.parsed.relatedPages)
            assertEquals(expectedPageImages, testQuery.parsed.imageNames)
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

            val exampleText = MY_EXAMPLE_TEXT
            val actualSummary = WebApi.meaningCloudApiService.getSummarizedText(txt =
                exampleText.toRequestBody(PLAINTEXT_MEDIA_TYPE), limit = 40).summary

            val expectedSummary = MY_EXAMPLE_TEXT_SUMMARY
            assertEquals(expectedSummary, actualSummary)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun test_summarize_tiny_example() = runTest {
            /** Test the MeaningCloudApiService's behaviour when the summary it retrieves
             * is very small. */

            val exampleText = "Word!"
            val actualSummary = WebApi.meaningCloudApiService.getSummarizedText(
                txt = exampleText.toRequestBody(PLAINTEXT_MEDIA_TYPE), limit = 40).summary
            val expectedSummary = "Word!"
            assertEquals(expectedSummary, actualSummary)
        }
    }

    class ArticleRepositoryTest {
        /** Normally I would attempt to test the getArticle() and queryRelatedPages functions in the repository, but because
         * the text retrieval only inputs for title, and pages on Wikipedia can often be edited,
         * automated, fixed-output testing for these functions is impossible.
         *
         * (Results given a title could change at any time).
         *
         * Instead I've included a test that will let me manually trace through to check. Not ideal,
         * but we play the cards we have.
         *
         * TODO: These seem to work for a basic case.
         */

        @Mock
        private lateinit var application: MyApplication
        private val articleDao = application.appDatabase.getArticleDao()
        private val imgUrlsDao = application.appDatabase.getImageUrlsDao()
        private val junctionDao = application.appDatabase.getJunctionsDao()

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun manual_test_getArticle() = runTest {
            val testTitle = "Gelmi"
            val finalArticle = ArticleRepository(articleDao, imgUrlsDao, junctionDao).getArticle(testTitle)
            assertEquals(finalArticle, finalArticle)
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun manual_test_queryRelatedPages() = runTest {
            val testTitle = "Gelmi"
            val relatedArticles = ArticleRepository(articleDao, imgUrlsDao, junctionDao).getRelatedPages(testTitle)
            assertEquals(relatedArticles, relatedArticles)
        }
    }

}

