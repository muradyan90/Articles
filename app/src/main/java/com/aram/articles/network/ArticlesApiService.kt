package com.aram.articles.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://content.guardianapis.com"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface ArticlesApiService{
    @GET("search?q=sport articles&page-size=10&show-fields=thumbnail,category&api-key=1841ef16-e404-452b-bde8-0ff6d3a3e332")
    fun getArticles(@Query("page") page: Int): Deferred<ResponsePage>

    @GET("search?q=sport articles&show-fields=thumbnail,category&api-key=1841ef16-e404-452b-bde8-0ff6d3a3e332")
    fun getNewArticles(@Query("page") page: Int,@Query("pageSize") pageSize: Int): Deferred<ResponsePage>
}

object ArticlesApi {
    val retrofitService : ArticlesApiService by lazy {
        retrofit.create(ArticlesApiService::class.java)
    }
}