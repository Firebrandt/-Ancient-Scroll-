package com.example.volumen.data

import androidx.room.*

@Dao
interface ArticleDao {
    /** A Room DAO that provides methods used to interact with the app's local SQLite room database.
     * Specifically the summarized_article table.
     */

    @Insert
    // Equivalent-ish to @Query("INSERT INTO summarized_article ([[article fields here]]))
    fun insert(article: Article) : Long

    @Update
    // Equivalent-ish to @Query("UPDATE summarized_article SET [relevant fields]
    // WHERE {all fields match parameter article}")
    fun update(article: Article)

    @Delete
    // Equivalent-ish to @Query("DELETE FROM summarized_article
    // WHERE {all fields match parameter article}")
    fun delete(article: Article)

    @Query("SELECT * FROM summarized_article WHERE title = :articleTitle LIMIT 1")
    fun findByTitle(articleTitle: String) : Article

    @Query("SELECT COUNT(*) FROM summarized_article")
    fun storedArticleCount() : Int

    @Query("SELECT * FROM summarized_article")
    fun selectAll() : List<Article>

    @Query("SELECT DISTINCT title FROM summarized_article")
    fun getTitlesInCache() : List<String>

    @Query("SELECT * FROM summarized_article WHERE id = :articleId")
    fun findById(articleId: Long) : Article

    @Query("DELETE FROM summarized_article")
    fun clearArticles()
}

@Dao
interface JunctionDao {
    /** A Room DAO that provides methods used to interact with the app's local SQLite database.
     * Specifically, the article_to_image_junction table.
     */

    @Query("SELECT contained_image_id FROM article_to_image_junction WHERE article_id = :articleId")
    fun findImageIdsByArticleId(articleId: Long) : List<Long>

    @Insert
    fun insertPairing(junction: ArticleImageJunction) : Long

    @Query("DELETE FROM article_to_image_junction")
    fun clearJunction()
}

@Dao
interface ImageUrlsDao {
    /** A Room DAO that provides methods used to interact with the app's local SQLite database.
     * Specifically, the image_urls table within it.
     */

    @Query("SELECT image_url FROM image_urls WHERE image_id = :imageUrlId")
    fun findImageUrlById(imageUrlId: Long) : String

    @Insert
    // The Long return type stores the ID of the inserted item.
    fun insertImageUrl(imageUrl: ImageUrl) : Long

    @Query("DELETE FROM image_urls")
    fun clearImages()
}