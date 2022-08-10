package com.example.volumen.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.gargoylesoftware.htmlunit.javascript.host.intl.Intl

@Entity(tableName = "summarized_article")
data class Article (
    /*** A dataclass modelling select summarized Wikipedia article attributes as rows
     * in a summarized_articles table. Note that we drop the links attribute; that one is mostly
     * used for searching an outline page (as a data source).
     *
     * Instance Attributes:
     * - id: A unique ID serving as a primary key.
     * - imageList: A list of URLs for images hosted online.
     * - summarized: The summarized text of the wikipedia article.
     * - title: The title of the Wikipedia Article.
     * - eventDate: The date of the historical event (if we can find it).
     * - originalUrl: The URL of the Wikipedia article, originally.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Keep this as a field, but ensure Room does not attempt to store it in the database directly.
    val summarized: String?,
    val title: String?,
    @ColumnInfo(name = "original_url") val originalURL: String?,
) {
    @Ignore var imageList: List<String>? = listOf()
    //TODO: For some reason putting an ignored attribute out of the constructor is necessary.
    // Why tho?
}

@Entity(tableName = "image_urls")
data class ImageUrl (
    /** A data class modelling a row of image Urls (for a wikipedia image in an article). **/
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "image_id") val imageId: Long = 0,
    @ColumnInfo(name = "image_url") val imageUrl: String
)

@Entity(tableName = "article_to_image_junction")
data class ArticleImageJunction(
    /** A data class modelling rows of a junction table used to store a list while following
     * the first principle of normalization in relational databases.
     *
     * The existence of a row with a particular id and image id means that article with {id}
     * has {image id} in its list of images.
     */
    @PrimaryKey(autoGenerate = true) val junctionId: Long = 0,
    @ColumnInfo(name = "article_id") val articleId : Long,
    @ColumnInfo(name = "contained_image_id") val imageId: Long
)