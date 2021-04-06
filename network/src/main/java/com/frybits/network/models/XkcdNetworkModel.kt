package com.frybits.network.models

import com.google.gson.annotations.SerializedName

data class XkcdNetworkModel(
    @SerializedName("num") val comicId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("alt") val alt: String,
    @SerializedName("img") val imgUrl: String,
    @SerializedName("month") val month: String,
    @SerializedName("day") val day: String,
    @SerializedName("year") val year: String
)