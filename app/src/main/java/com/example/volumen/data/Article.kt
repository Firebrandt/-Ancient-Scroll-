package com.example.volumen.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "images") val imageList: List<String>?,
    val summarized: String?,
    val title: String?,
    @ColumnInfo(name = "original_url") val originalURL: String?
)