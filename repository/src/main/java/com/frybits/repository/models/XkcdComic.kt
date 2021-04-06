package com.frybits.repository.models

import com.frybits.local.model.XkcdLocalModel
import com.frybits.network.models.XkcdNetworkModel
import java.util.*

data class XkcdComic(
    val comicId: Int,
    val title: String,
    val alt: String,
    val date: Date,
    internal val imgUrl: String
)

fun XkcdLocalModel.toComic(): XkcdComic = XkcdComic(
    comicId,
    title,
    alt,
    Calendar.getInstance().apply {
        set(year.toInt(), month.toInt(), day.toInt())
    }.time,
    imgUrl
)

fun XkcdNetworkModel.toComic(): XkcdComic = XkcdComic(
    comicId,
    title,
    alt,
    Calendar.getInstance().apply {
        set(year.toInt(), month.toInt(), day.toInt())
    }.time,
    imgUrl
)