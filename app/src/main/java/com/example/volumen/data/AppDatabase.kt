package com.example.volumen.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Article::class, ImageUrl::class, ArticleImageJunction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    /** A class to define the configuration of the App's Room Database, and provide instances
     * of its DAO(s) to allow the rest of the app an access point to persisted data.
     *
     * Note that this class is abstract because Room will generate an implementation for us
     * auto-magically.
     */
    abstract fun getArticleDao() : ArticleDao

    abstract fun getImageUrlsDao(): ImageUrlsDao

    abstract fun getJunctionsDao(): JunctionDao

    companion object {

        // @Volatile essentially makes sure INSTANCE is super up to date.
        // Changes from 1 thread will be visible to others immediately. Helps fight bugs.
        @Volatile
        var databaseInstance : AppDatabase? = null

        fun getDatabase(context: Context) : AppDatabase {
            /** Return an instance of the database, if we haven't made one already. Otherwise
             * return the existing instance.
             */
            return databaseInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app-database")
                    // We aren't expecting to have to do database migration
                    // since we use this for caching only.
                    .fallbackToDestructiveMigration()
                    .build()

                databaseInstance = instance
                return instance
            }
        }

    }
}