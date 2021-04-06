package com.frybits.repository.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import com.frybits.network.XkcdNetwork
import com.frybits.repository.models.XkcdComic
import com.jakewharton.disklrucache.DiskLruCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val DISK_CACHE_SIZE = 1024 * 1024 * 10L // 10MB
private const val DISK_CACHE_SUBDIR = "xkcdcomics"

@Singleton
internal class ComicImageCache internal constructor(
    private val context: Context,
    private val xkcdNetwork: XkcdNetwork,
    private val mainDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher
) {

    @Inject internal constructor(
        @ApplicationContext context: Context,
        xkcdNetwork: XkcdNetwork
    ): this(context, xkcdNetwork, Dispatchers.Main, Dispatchers.IO)

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private lateinit var diskLruCache: DiskLruCache

    // Not so simple suspend function that handles loading from memory if bitmap exists, disk cache if it was already stored, or network if it hasn't been fetched yet.
    // Also handles caching as necessary!
    suspend fun getComicImageForId(xkcdComic: XkcdComic): Bitmap? {
        return supervisorScope { // Supervisor scope as we don't want exceptions from IO killing the coroutine calling this.
            try {
                return@supervisorScope withContext(mainDispatcher) { // Move to main thread, since we are manipulating memory. No need for "synchronize" here.
                    val memoryBitmap = memoryCache[xkcdComic.comicId.toString()] // Read memory cache for bitmap...

                    if (memoryBitmap != null) {
                        return@withContext memoryBitmap
                    }

                    // Bitmap was not in memory! Loading it from disk...
                    if (!::diskLruCache.isInitialized) {
                        // We are checking if "diskLruCache" is already initialized in the main thread. If not, initialize it in IO thread, then assign the variable in main thread.
                        diskLruCache = withContext(ioDispatcher) { // Move to IO thread for IO calls!
                            DiskLruCache.open(
                                File(context.cacheDir, DISK_CACHE_SUBDIR),
                                1,
                                20,
                                DISK_CACHE_SIZE
                            )
                        }
                    }

                    val cachedBitmap = withContext(ioDispatcher) io@{ // Getting cached bitmap from disk in IO thread...
                        try {
                            val inputStream =
                                diskLruCache[xkcdComic.comicId.toString()]?.getInputStream(0)
                                    ?: return@io null
                            return@io BitmapFactory.decodeStream(inputStream)
                        } catch (e: IOException) {
                            null
                        }
                    }

                    // Back on main thread here. Putting bitmap into memory cache if one was found!
                    if (cachedBitmap != null) {
                        memoryCache.put(xkcdComic.comicId.toString(), cachedBitmap)
                        return@withContext cachedBitmap
                    }

                    val networkBitmap = withContext(ioDispatcher) cache@{ // Going back to an IO thread...
                        val bitmap = BitmapFactory.decodeStream(
                            xkcdNetwork.getComicImage(xkcdComic.imgUrl).byteStream() // The network call doesn't need to be in an IO thread, since it is a suspend function, but the decoding of the response does have to be.
                        ) ?: return@cache null

                        // Write the image to the disk!
                        val editor =
                            diskLruCache[xkcdComic.comicId.toString()]?.edit()
                                ?: return@cache bitmap
                        val bufferedOutputStream = editor.newOutputStream(0).buffered()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bufferedOutputStream)
                        bufferedOutputStream.flush()
                        editor.commit()
                        return@cache bitmap
                    } ?: return@withContext null

                    // Back in main thread! Store bitmap to memory cache.
                    memoryCache.put(xkcdComic.comicId.toString(), networkBitmap)

                    return@withContext networkBitmap
                }
            } catch (e: Exception) { // If at any point there was an exception, capture it here. Since it was thrown in a supervisor scope, calling coroutine won't be cancelled.
                return@supervisorScope null
            }
        }
    }
}