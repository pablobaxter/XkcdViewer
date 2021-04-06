package com.frybits.repository.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frybits.local.dao.XkcdDao
import com.frybits.local.model.XkcdLocalModel
import com.frybits.network.XkcdNetwork
import com.frybits.network.models.XkcdNetworkModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalPagingApi::class)
@Singleton
internal class XkcdMediator @Inject constructor(
    private val xkcdDao: XkcdDao,
    private val xkcdNetwork: XkcdNetwork
) : RemoteMediator<Int, XkcdLocalModel>() {

    private var currentRemoteComic: XkcdLocalModel? = null

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XkcdLocalModel>
    ): MediatorResult {

        // This block can be called on any thread. But given it is a suspend function, I don't care, as I'll swap context as needed for any work I require in a specific thread

        return supervisorScope {
            try {
                val loadKey = when(loadType) {
                    LoadType.REFRESH -> {
                        // Network call. The function is a suspend function, so it is safe to call directly.
                        currentRemoteComic = xkcdNetwork.getCurrentComic().toLocalModel()
                        null
                    }
                    LoadType.PREPEND -> {
                        return@supervisorScope MediatorResult.Success(endOfPaginationReached = true)
                    }
                    LoadType.APPEND -> {
                        var latestComic: XkcdLocalModel? = currentRemoteComic
                        if (latestComic == null) {
                            // Network call. The function is a suspend function, so it is safe to call directly.
                            latestComic = xkcdNetwork.getCurrentComic().toLocalModel()
                            currentRemoteComic = latestComic
                        }

                        if (latestComic == state.lastItemOrNull() || latestComic.comicId == state.anchorPosition) {
                            return@supervisorScope MediatorResult.Success(endOfPaginationReached = true)
                        }
                        state.lastItemOrNull()?.comicId
                    }
                }

                val deferredJobs = arrayListOf<Deferred<XkcdLocalModel>>()
                val currentId = (loadKey?: 0) + 1
                repeat(state.config.pageSize) {
                    // Launching many network calls at once...
                    deferredJobs.add(async { xkcdNetwork.getComic(currentId + it).toLocalModel() })
                }
                // Awaiting for result of all network calls, then pushing them into the local database.
                // Network calls + database calls are suspend functions, so they are safe to call directly.
                xkcdDao.insertComics(deferredJobs.awaitAll())
                MediatorResult.Success(endOfPaginationReached = false)
            } catch (e: Exception) {
                MediatorResult.Error(e)
            }
        }
    }
}

private fun XkcdNetworkModel.toLocalModel(): XkcdLocalModel = XkcdLocalModel(comicId, title, alt, imgUrl, month, day, year)