package com.aram.articles.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.QueryMap


interface ArticlesApiService {
    @GET("search")
    fun getArticles(
        @QueryMap queryParams: MutableMap<String, String>
    ): Single<ResponsePage>
}

