package com.aram.articles

import androidx.room.Room
import com.aram.articles.database.ArticleDatabase
import com.aram.articles.network.ArticlesApiService
import com.aram.articles.repository.ArticlesRepository
import com.aram.articles.viewmodels.AllArticlesViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


private const val BASE_URL = "https://content.guardianapis.com"

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            ArticleDatabase::class.java,
            "articles_database"
        ).fallbackToDestructiveMigration()
            .build()
    }
    single {
        ArticlesRepository(
            androidApplication(),
            get<ArticleDatabase>().articlesDao
        )
    }

    single<Moshi> {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }


    single<Retrofit> {
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(get()))
            //.addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    single<ArticlesApiService> { get<Retrofit>().create(ArticlesApiService::class.java) }

    viewModel {
        AllArticlesViewModel(
            androidApplication(),
            get<ArticleDatabase>().articlesDao,
            get<ArticleDatabase>().tappedArticlesDao
        )
    }
}