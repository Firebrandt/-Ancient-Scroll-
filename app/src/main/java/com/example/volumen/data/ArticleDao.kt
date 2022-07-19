package com.example.volumen.data

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

interface ArticleDao {
    /** A Room DAO that provides methods used to interact with the app's local SQLite room database.
     * Includes support for INSERT, UPDATE, DELETE, and some SELECT queries.
     */

    @Insert
    fun insert(article: Article)

    @Update
    fun update(article: Article)

    @Delete
    fun delete(article: Article)

}