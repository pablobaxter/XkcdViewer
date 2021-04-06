package com.frybits.network

import com.frybits.network.models.XkcdNetworkModel
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface XkcdNetwork {

    @GET("info.0.json")
    suspend fun getCurrentComic(): XkcdNetworkModel

    @GET("{comicId}/info.0.json")
    suspend fun getComic(@Path("comicId") id: Int): XkcdNetworkModel

    @GET
    suspend fun getComicImage(@Url url: String): ResponseBody
}
