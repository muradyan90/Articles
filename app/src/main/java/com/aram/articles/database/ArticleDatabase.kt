package com.aram.articles.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ArticleEntity::class, TappedArticleEntity::class], version = 1, exportSchema = false)
abstract class ArticleDatabase : RoomDatabase() {

    abstract val articlesDao: ArticlesDao
    abstract val tappedArticlesDao: TappedArticleDao

    // BAD PRACTICE MUST BE REPLACED WITH DI
    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null

        fun getInstance(context: Context): ArticleDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ArticleDatabase::class.java,
                        "articles_database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}