package com.frybits.repository

import android.graphics.Bitmap
import androidx.paging.*
import com.frybits.local.dao.XkcdDao
import com.frybits.repository.images.ComicImageCache
import com.frybits.repository.mediator.XkcdMediator
import com.frybits.repository.models.XkcdComic
import com.frybits.repository.models.toComic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XkcdRepository @Inject internal constructor(
    private val xkcdDao: XkcdDao,
    private val xkcdMediator: XkcdMediator,
    private val comicImageCache: ComicImageCache
) {

    // Flow that handles loading all comic data. Does not have to be suspend.
    @OptIn(ExperimentalPagingApi::class)
    fun pagedFlow(offset: Int = 1, pageSize: Int = 20): Flow<PagingData<XkcdComic>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            prefetchDistance = 2
        ),
        initialKey = offset,
        remoteMediator = xkcdMediator
    ) {
        xkcdDao.pagingSource()
    }.flow.map { pagingData ->
        pagingData.map { it.toComic() }
    }

    // Function that loads the comic image from memory/storage/network. Suspend function, so safe to call directly.
    suspend fun getComicImage(xkcdComic: XkcdComic): Bitmap? {
        return comicImageCache.getComicImageForId(xkcdComic)
    }
}