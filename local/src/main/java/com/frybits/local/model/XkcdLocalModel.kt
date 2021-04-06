package com.frybits.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class XkcdLocalModel(
    @PrimaryKey val comicId: Int,
    val title: String,
    val alt: String,
    val imgUrl: String,
    val month: String,
    val day: String,
    val year: String
)